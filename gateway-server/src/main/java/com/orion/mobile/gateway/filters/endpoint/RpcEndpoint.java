package com.orion.mobile.gateway.filters.endpoint;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import org.slf4j.LoggerFactory;
import com.orion.mobile.gateway.domain.GatewayResponse;
import com.orion.mobile.gateway.domain.ServiceInstance;
import com.orion.mobile.gateway.domain.StaticInfo;
import com.orion.mobile.gateway.service.ScheduleService;
import com.orion.mobile.gateway.service.ServiceDefineCacheService;
import com.orion.mobile.gateway.service.ServiceInvokeService;
import com.orion.mobile.gateway.util.GatewayConstants;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Singleton
public class RpcEndpoint extends HttpSyncEndpoint {
    private static final Logger log = LoggerFactory.getLogger(RpcEndpoint.class);

    @Inject
    ServiceDefineCacheService serviceDefineCacheService;
    @Inject
    ServiceInvokeService serviceInvokeService;

    @Inject
    ScheduleService scheduleService;

    @PostConstruct
    public void init() {
        final AbstractConfiguration configInstance = ConfigurationManager.getConfigInstance();
        boolean printStaic = configInstance.getBoolean("printStatic", false);
        if (printStaic) {
            log.info("start static service");
            scheduleService.scheduleAtFixedRate(() -> printStaticInfo(), 0, 1, TimeUnit.MINUTES);
        }
    }

    Map<ServiceInstance, StaticInfo> staticMap = Maps.newConcurrentMap();

    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        SessionContext context = request.getContext();

        String serviceDomain = context.getString(GatewayConstants.SERVICE_DOMAIN);
        String serviceName = context.getString(GatewayConstants.SERVICE_NAME);
        String serviceMethod = context.getString(GatewayConstants.SERVICE_METHOD);
        String serviceVersion = context.getString(GatewayConstants.MESSAGE_VERSION);
        String token = context.getString(GatewayConstants.MESSAGES_TOKEN);
        String action = context.getString(GatewayConstants.MESSAGE_ACTION);
        String deviceToken= context.getString(GatewayConstants.MESSAGE_DEVICE);
        ServiceInstance serviceInstance = new ServiceInstance(serviceName, serviceDomain, serviceMethod, serviceVersion);

        StaticInfo staticInfo = init(staticMap, serviceInstance);


        boolean serviceValidate = serviceDefineCacheService.isServiceValidate(serviceInstance);
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setVersion(serviceVersion);
        HttpResponseMessage resp = new HttpResponseMessageImpl(request.getContext(), request, 200);
        if (!serviceValidate) {
            log.error("not found validate service info {} {}  {} {} ", serviceDomain, serviceName, serviceMethod, serviceVersion);
            gatewayResponse.setCode("500");
            gatewayResponse.setMessage("not found validate service info ");
            gatewayResponse.setSuccess(false);
            staticInfo.validateFail();
        } else {
            gatewayResponse = new GatewayResponse();
            gatewayResponse.setVersion(serviceVersion);
            try {
                long l = System.currentTimeMillis();
                Object responseObj = serviceInvokeService.invoke(serviceInstance,deviceToken, token, (JSONObject) context.get(GatewayConstants.MESSAGE_BODY));
                log.info("GATEWAY_RPC_INVOKE {} {} {} {}", action,deviceToken, token, System.currentTimeMillis() - l);
                setResponse(gatewayResponse, responseObj);
                staticInfo.suc();
            } catch (Throwable e) {
                staticInfo.excep();
                log.error("invoke service  exception " + JSON.toJSONString(serviceInstance), e);
                gatewayResponse.setCode("500");
                gatewayResponse.setMessage("invoke service exception ");
                String env_name = System.getenv("env_name") == null ? "dev" : System.getenv("env_name");
                if (!StringUtils.equals(env_name, "prod")) {
                    gatewayResponse.setMessage(e.getMessage());
                }
                gatewayResponse.setSuccess(false);
            }
        }
        resp.setBodyAsText(JSON.toJSONString(gatewayResponse, SerializerFeature.DisableCircularReferenceDetect));
        StatusCategoryUtils.setStatusCategory(request.getContext(), ZuulStatusCategory.SUCCESS);
        return resp;
    }

    /**
     * print static info
     */
    private void printStaticInfo() {
        try {
            Iterator<ServiceInstance> iterator = staticMap.keySet().iterator();
            while (iterator.hasNext()) {
                ServiceInstance serviceInstance = iterator.next();
                StaticInfo staticInfo = staticMap.get(serviceInstance);
                long l = System.currentTimeMillis();
                long l1 = l / 1000 / 60;
                if (staticInfo.needPrint((int) l1)) {
                    log.info("service {}_{}_{}_{} static  - suc {} - fail {} - excep {} ", serviceInstance.getAppName(), serviceInstance.getServiceName(), serviceInstance.getServiceMethod(), serviceInstance.getServiceVersion(), staticInfo.getSuc(), staticInfo.getValidateFail(), staticInfo.getExcep());
                }
            }
        } finally {

        }
    }

    /**
     * init static info
     *
     * @param staticMap
     * @param serviceDomain
     * @return
     */
    private StaticInfo init(Map<ServiceInstance, StaticInfo> staticMap, ServiceInstance serviceDomain) {
        if (staticMap.containsKey(serviceDomain)) {
            return staticMap.get(serviceDomain);
        }
        synchronized (RpcEndpoint.class) {
            if (staticMap.containsKey(serviceDomain)) {
                return staticMap.get(serviceDomain);
            }
            StaticInfo staticInfo = new StaticInfo();
            staticMap.put(serviceDomain, staticInfo);
            return staticInfo;
        }
    }

    private void setResponse(GatewayResponse gatewayResponse, Object responseObj) {
        if (!(gatewayResponse instanceof Object)) {
            return;
        }
        String code = "200";
        String message = "success";
        Object data = responseObj;
        Boolean success = true;
        if (responseObj instanceof Map) {
            Map map = (Map) responseObj;
            code = MapUtils.getString(map, "code");
            message = extractMessageFromMap(map);
            data = MapUtils.getObject(map, "data");
            success = MapUtils.getBoolean(map, "success");
        }
        //
        gatewayResponse.setData(data);
        gatewayResponse.setCode(code);
        gatewayResponse.setMessage(message);
        gatewayResponse.setSuccess(success);
    }

    private String extractMessageFromMap(Map map) {
        String message = MapUtils.getString(map, "message");
        if (message == null) {
            message = MapUtils.getString(map, "errorMessage");
        }
        if (message == null) {
            message = MapUtils.getString(map, "msg");
        }
        return message;
    }

    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        String path = StringUtils.trim(StringUtils.removeStart(msg.getPath(), "/"));
        if (!StringUtils.equalsAnyIgnoreCase("rpc", path)) {
            return false;
        }
        return true;
    }
}
