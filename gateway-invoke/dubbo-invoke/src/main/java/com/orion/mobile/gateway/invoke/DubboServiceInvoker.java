package com.orion.mobile.gateway.invoke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.netflix.config.ConfigurationManager;
import com.orion.mobile.gateway.util.UUIDUtils;
import org.slf4j.LoggerFactory;
import com.orion.mobile.gateway.domain.RpcServiceInfo;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;
import com.orion.mobile.gateway.service.ServiceParamBuildService;
import com.orion.mobile.gateway.util.GatewayConstants;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/31 20:53
 * @Version 1.0.0
 */
public class DubboServiceInvoker implements ServiceInvoker {

    private Logger logger = LoggerFactory.getLogger(DubboServiceInvoker.class);

    Map<String, Pair<Long, GenericService>> serviceMap = Maps.newConcurrentMap();

    private final ApplicationConfig applicationConfig = new ApplicationConfig();
    private final RegistryConfig registryConfig = new RegistryConfig();

    @Inject
    private ServiceParamBuildService serviceParamBuildService;

    private Integer retry;
    private int timeout;

    @PostConstruct
    public void init() {
        final AbstractConfiguration configInstance = ConfigurationManager.getConfigInstance();
        applicationConfig.setId(configInstance.getString("dubbo.application"));
        applicationConfig.setName(configInstance.getString("dubbo.application"));
        registryConfig.setAddress(configInstance.getString("dubbo.register.addr"));
        registryConfig.setProtocol(configInstance.getString("dubbo.register.protocol"));
        retry = configInstance.getInteger("dubbo.retry", 0);
        timeout = configInstance.getInteger("dubbo.timeout", 3000);
    }

    public GenericService getDubboService(String interfaceName, String version, String group) {
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(interfaceName);
        referenceConfig.setVersion(version);
        referenceConfig.setGroup(group);
        referenceConfig.setApplication(applicationConfig);
        referenceConfig.setRegistry(registryConfig);
        referenceConfig.setGeneric(true);
        referenceConfig.setTimeout(timeout);
        referenceConfig.setRetries(retry);
        return referenceConfig.get();
    }

    @Override
    public Object invoke(RpcServiceInfo rpcServiceInfo, String deviceToken, String userToken, RpcServiceMethodInfo rpcServiceMethodInfo, JSONObject messageBody) throws Exception {
        GenericService service = getService(rpcServiceInfo);
        String[] paramType = serviceParamBuildService.buildParamType(rpcServiceMethodInfo);
        Object o = null;
        Object[] value = serviceParamBuildService.buildParamValue(rpcServiceMethodInfo.getParamFieldList(), messageBody);
        try {
            if (StringUtils.isNotEmpty(userToken)) {
                RpcContext.getContext().setAttachment(GatewayConstants.MESSAGES_TOKEN, userToken);
                RpcContext.getContext().setAttachment(GatewayConstants.MESSAGE_DEVICE, deviceToken);
            } else {
                logger.warn("request {} accessToken is null", JSON.toJSONString(messageBody));
            }
            RpcContext.getContext().set("_trace_id", UUIDUtils.next());
            o = service.$invoke(rpcServiceMethodInfo.getMethodName(), paramType, value);
            return o;
        } catch (Throwable e) {
            logger.warn("rpc invoke {} {} {}", JSON.toJSONString(value), JSON.toJSONString(o), JSON.toJSONString(rpcServiceMethodInfo));
            RpcContext.getContext().remove(GatewayConstants.MESSAGES_TOKEN);
            RpcContext.getContext().remove(GatewayConstants.MESSAGE_DEVICE);
            throw e;
        }
    }

    /**
     * get service
     *
     * @param rpcServiceInfo
     * @return
     */
    private GenericService getService(RpcServiceInfo rpcServiceInfo) {
        String serviceName = rpcServiceInfo.getConfigValue("interface");
        String group = rpcServiceInfo.getConfigValue("group");
        String version = rpcServiceInfo.getConfigValue("version");

        String serviceKey = String.format("%s_%s_%s", serviceName, group, version);
        Long timestamp = rpcServiceInfo.getTimestamp();
        if (serviceMap.containsKey(serviceKey) && serviceMap.get(serviceKey).getLeft().equals(timestamp)) {
            return serviceMap.get(serviceKey).getRight();
        }
        synchronized (DubboServiceInvoker.class) {
            if (!serviceMap.containsKey(serviceKey)) {
                serviceMap.put(serviceKey, Pair.of(timestamp, getDubboService(serviceName, version, group)));
            }
        }
        return serviceMap.get(serviceKey).getRight();
    }

    @Override
    public String code() {
        return "DUBBO";
    }
}
