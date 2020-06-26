package com.orion.mobile.gateway.invoke;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orion.mobile.gateway.util.UUIDUtils;
import org.slf4j.LoggerFactory;
import com.orion.mobile.gateway.domain.RpcServiceInfo;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;
import com.orion.mobile.gateway.service.ServiceParamBuildService;
import com.orion.mobile.gateway.spring.GatewaySpringContextUtils;
import com.orion.mobile.gateway.util.InvokeParamContext;
import com.orion.mobile.gateway.util.LocalParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/13 11:53
 * @Version 1.0.0
 */
@Singleton
public class LocalSpringServiceInvoker implements ServiceInvoker {

    private static Logger logger = LoggerFactory.getLogger(LocalSpringServiceInvoker.class);
    Map<String, Pair<Long, LocalServiceTarget>> serviceMap = Maps.newConcurrentMap();

    @Inject
    private ServiceParamBuildService serviceParamBuildService;

    @Override
    public Object invoke(RpcServiceInfo rpcServiceInfo, String deviceToken,String userToken, RpcServiceMethodInfo rpcServiceMethodInfo, JSONObject messageBody) throws Exception {
        try {
            LocalServiceTarget service = getService(rpcServiceInfo, rpcServiceMethodInfo);
            if (service == null) {
                throw new RuntimeException("not found local service " + rpcServiceInfo.getServiceConfig());
            }
            Object[] value = serviceParamBuildService.buildParamValue(rpcServiceMethodInfo.getParamFieldList(), messageBody);
            if (service.getParamTypes().length != value.length) {
                throw new RuntimeException("service define not validate " + JSON.toJSONString(rpcServiceMethodInfo.getMethodName()));
            }
            processWithClassInfo(service.getParamTypes(), value);
            InvokeParamContext.set(LocalParam.of(userToken, UUIDUtils.next()));
            return ReflectionUtils.invokeMethod(service.getMethod(), service.getService(), value);
        } catch (Throwable e) {
            InvokeParamContext.remove();
            logger.warn("rpc invoke {} {} {}", JSON.toJSONString(rpcServiceInfo.getServiceConfig()), JSON.toJSONString(rpcServiceMethodInfo.getMethodAlias()), JSON.toJSONString(messageBody));
            throw e;
        }
    }

    public void processWithClassInfo(Type[] paramterTypes, Object[] value) throws Exception {
        for (int i = 0; i < value.length; i++) {
            if (value[i] != null) {
                value[i] = JSON.parseObject(JSON.toJSONString(value[i]), paramterTypes[i]);
            }
        }
    }

    /**
     * get service
     *
     * @param rpcServiceInfo
     * @return
     */
    private LocalServiceTarget getService(RpcServiceInfo rpcServiceInfo, RpcServiceMethodInfo rpcServiceMethodInfo) throws Exception {
        String serviceName = rpcServiceInfo.getConfigValue("beanName");

        String serviceKey = String.format("%s_%s_%s", serviceName, rpcServiceMethodInfo.getMethodAlias(), rpcServiceInfo.getServiceVersion());
        Long timestamp = rpcServiceInfo.getTimestamp();
        if (serviceMap.containsKey(serviceKey) && serviceMap.get(serviceKey).getLeft().equals(timestamp)) {
            return serviceMap.get(serviceKey).getRight();
        }
        synchronized (LocalSpringServiceInvoker.class) {
            if (!serviceMap.containsKey(serviceKey)) {
                Object bean = GatewaySpringContextUtils.getBean(serviceName);
                Method method = getMethod(bean, rpcServiceMethodInfo);
                if (method == null) {
                    throw new RuntimeException("method not found " + rpcServiceMethodInfo.getMethodName() + " by type " + JSON.toJSONString(rpcServiceMethodInfo.getParamFieldList()));
                }
                LocalServiceTarget localServiceTarget = new LocalServiceTarget();
                localServiceTarget.setMethod(method);
                localServiceTarget.setService(bean);
                localServiceTarget.setParamTypes(localServiceTarget.getMethod().getGenericParameterTypes());
                serviceMap.put(serviceKey, Pair.of(timestamp, localServiceTarget));
            }
        }
        return serviceMap.get(serviceKey).getRight();
    }

    private Method getMethod(Object bean, RpcServiceMethodInfo rpcServiceMethodInfo) throws Exception {
        if (bean == null || StringUtils.isEmpty(rpcServiceMethodInfo.getMethodName())) {
            return null;
        }
        String[] strings = serviceParamBuildService.buildParamType(rpcServiceMethodInfo);
        Class[] paramTypes = new Class[strings.length];
        if (strings.length > 0) {
            for (int i = 0; i < strings.length; i++) {
                paramTypes[i] = Class.forName(strings[i]);
            }
        }
        Class<?> aClass = bean.getClass();
        Method method = MethodUtils.getMatchingMethod(aClass, rpcServiceMethodInfo.getMethodName(), paramTypes);
        return method;
    }

    @Override
    public String code() {
        return "LOCAL-SPRING";
    }

}
