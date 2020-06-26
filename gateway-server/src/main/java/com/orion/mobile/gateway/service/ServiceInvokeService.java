package com.orion.mobile.gateway.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.slf4j.LoggerFactory;
import com.orion.mobile.gateway.domain.ReturnConfig;
import com.orion.mobile.gateway.domain.RpcServiceInfo;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;
import com.orion.mobile.gateway.domain.ServiceInstance;
import com.orion.mobile.gateway.invoke.ServiceInvoker;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.*;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/31 19:32
 * @Version 1.0.0
 */
@Singleton
public class ServiceInvokeService {
    private Logger logger = LoggerFactory.getLogger(ServiceInvokeService.class);

    @Inject
    ServiceDefineCacheService serviceDefineCacheService;

    @Inject
    @Named("DUBBO")
    ServiceInvoker dubboInvoker;
    @Inject
    @Named("LOCAL-SPRING")
    ServiceInvoker springLocalInvoker;

    public Object invoke(ServiceInstance serviceInstance,String deviceToken, String token, JSONObject messageBody) throws Exception {
        RpcServiceInfo serviceInfo = serviceDefineCacheService.getServiceInfo(serviceInstance);
        RpcServiceMethodInfo serviceMethodInfo = serviceDefineCacheService.getServiceMethodInfo(serviceInfo.getId(), serviceInstance);
        Object response = null;
        switch (serviceInfo.getServiceType()) {
            case "DUBBO":
                response = dubboInvoker.invoke(serviceInfo, deviceToken,token, serviceMethodInfo, messageBody);
                return processDubbo(response, serviceMethodInfo.getReturnConfig());
            case "LOCAL-SPRING":
                response = springLocalInvoker.invoke(serviceInfo,deviceToken, token, serviceMethodInfo, messageBody);
                return processLocal(response, serviceMethodInfo.getReturnConfig());
        }
        return null;
    }

    private Object processDubbo(Object response, ReturnConfig returnConfig) {
        if (!(response instanceof Map)) {
            return response;
        }
        if (!(response instanceof Map) || returnConfig == null || (returnConfig.getCodeRef() == null && returnConfig.getDataRef() == null && returnConfig.getMsgRef() == null && returnConfig.getSucRef() == null)) {
            Map<String, Object> returnV = Maps.newHashMap();
            returnV.put("data", response);
            returnV.put("success", true);
            return returnV;
        }
        return process((Map) response, returnConfig);
    }

    private Object processLocal(Object response, ReturnConfig returnConfig) {
        if (returnConfig == null || (returnConfig.getCodeRef() == null && returnConfig.getDataRef() == null && returnConfig.getMsgRef() == null && returnConfig.getSucRef() == null)) {
            Map<String, Object> returnV = Maps.newHashMap();
            returnV.put("data", response);
            returnV.put("success", true);
            return returnV;
        }
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(response));
        return process(jsonObject, returnConfig);
    }

    /**
     * process result
     *
     * @param response
     * @param returnConfig
     * @return
     */
    public Object process(Map<String, Object> response, ReturnConfig returnConfig) {
        Map<String, Object> re = new HashMap();
        if (CollectionUtils.isNotEmpty(returnConfig.getDataRef())) {
            Map<String, Object> tmpMap = new HashMap<>();
            for (String s : returnConfig.getDataRef()) {
                if (StringUtils.isEmpty(s)) {
                    continue;
                }
                if (StringUtils.startsWith(s, "$")) {
                    String key = s.substring(1);
                    Object o = response.get(key);
                    removeClassInfo(o);
                    if (o instanceof Map) {
                        tmpMap.putAll((Map) o);
                    } else {
                        tmpMap.put(key, o);
                    }
                } else {
                    Object o = response.get(s);
                    removeClassInfo(o);
                    tmpMap.put(s, o);
                }
            }
            re.put("data", tmpMap);
        } else {
            Object o = response.get(returnConfig.getDataRef());
            removeClassInfo(o);
            re.put("data", o);
        }
        re.put("success", response.get(returnConfig.getSucRef()));
        re.put("msg", response.get(returnConfig.getMsgRef()));
        re.put("code", response.get(returnConfig.getCodeRef()));
        return re;
    }

    private void removeClassInfo(Object o) {
        if (o != null && o instanceof Map) {
            Map o1 = (Map) o;
            o1.remove("class");
            Iterator iterator = o1.keySet().iterator();
            while (iterator.hasNext()) {
                Object next = iterator.next();
                Object o2 = o1.get(next);
                removeClassInfo(o2);
            }
        } else if (o != null && o instanceof List) {
            List o1 = (List) o;
            for (Object o2 : o1) {
                removeClassInfo(o2);
            }
        }
    }

}
