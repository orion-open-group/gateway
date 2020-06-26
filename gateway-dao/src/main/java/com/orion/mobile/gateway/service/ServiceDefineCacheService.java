package com.orion.mobile.gateway.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.orion.mobile.gateway.domain.RpcServiceInfo;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;
import com.orion.mobile.gateway.domain.ServiceInstance;
import com.orion.mobile.gateway.enums.ServiceOperType;
import com.orion.mobile.gateway.repository.ServiceLoader;
import com.orion.mobile.gateway.listener.ServiceListener;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * can not be use for spring,it's use for guice
 *
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/31 8:41
 * @Version 1.0.0
 */
@Singleton
public class ServiceDefineCacheService {

    @Inject
    ServiceLoader serviceLoader;
    Map<String, Map<String, RpcServiceInfo>> rpcServiceInfoMap = Maps.newConcurrentMap();
    Map<Long, Map<String, RpcServiceMethodInfo>> rpcServiceMethodInfoMap = Maps.newConcurrentMap();
    private Logger logger = LoggerFactory.getLogger(ServiceDefineCacheService.class);

    @PostConstruct
    public void initServiceAndMethod() throws Exception {
        List<RpcServiceInfo> rpcServiceInfoList = serviceLoader.listAllServiceInfo(null);
        if (CollectionUtils.isNotEmpty(rpcServiceInfoList)) {
            rpcServiceInfoList.stream().forEach(rpcServiceInfo -> addService(rpcServiceInfo));
        }
        List<RpcServiceMethodInfo> rpcServiceMethodInfoList = serviceLoader.listAllServiceMethodByApp(null);
        if (CollectionUtils.isNotEmpty(rpcServiceMethodInfoList)) {
            rpcServiceMethodInfoList.stream().forEach(rpcServiceMethodInfo -> addServiceMethod(rpcServiceMethodInfo));
        }
        ServiceListener serviceListener = new ServiceListener() {
            @Override
            public void notifyServiceChange(ServiceOperType serviceOperType, RpcServiceInfo rpcServiceInfo) {
                switch (serviceOperType) {
                    case ADD:
                        addService(rpcServiceInfo);
                        break;
                    case DELETE:
                        deleteService(rpcServiceInfo);
                        break;
                    case MODIFY:
                        modifyService(rpcServiceInfo);
                        break;
                    case DISABLE:
                        disableService(rpcServiceInfo);
                        break;
                    case ENABLE:
                        enalbeService(rpcServiceInfo);
                        break;
                }
            }

            @Override
            public void notifyServiceMethodChange(ServiceOperType serviceOperType, RpcServiceMethodInfo rpcServiceMethodInfo) {
                switch (serviceOperType) {
                    case ADD:
                        addServiceMethod(rpcServiceMethodInfo);
                        break;
                    case DELETE:
                        deleteServiceMethod(rpcServiceMethodInfo);
                        break;
                    case MODIFY:
                        modifyServiceMethod(rpcServiceMethodInfo);
                        break;
                    case DISABLE:
                        disableServiceMethod(rpcServiceMethodInfo);
                        break;
                    case ENABLE:
                        enalbeServiceMethod(rpcServiceMethodInfo);
                        break;
                }
            }
        };
        serviceLoader.registerListener(serviceListener);
    }

    private void enalbeServiceMethod(RpcServiceMethodInfo rpcServiceMethodInfo) {
        Long serviceId = rpcServiceMethodInfo.getServiceId();
        if (!rpcServiceMethodInfoMap.containsKey(serviceId) || rpcServiceMethodInfoMap.get(serviceId).containsKey(rpcServiceMethodInfo.getMethodAlias())) {
            logger.info("service or serviceMethod not in the cache {} for enable", JSON.toJSONString(rpcServiceMethodInfo));
            return;
        }
        logger.info("service method enable {} ",JSON.toJSONString(rpcServiceMethodInfo));
        rpcServiceMethodInfoMap.get(serviceId).get(rpcServiceMethodInfo.getMethodAlias()).setIsValid(true);
    }

    private void disableServiceMethod(RpcServiceMethodInfo rpcServiceMethodInfo) {
        Long serviceId = rpcServiceMethodInfo.getServiceId();
        if (!rpcServiceMethodInfoMap.containsKey(serviceId) || rpcServiceMethodInfoMap.get(serviceId).containsKey(rpcServiceMethodInfo.getMethodAlias())) {
            logger.info("service or serviceMethod not in the cache {} for disable ", JSON.toJSONString(rpcServiceMethodInfo));
            return;
        }
        logger.info("service method disabled  {} ",JSON.toJSONString(rpcServiceMethodInfo));
        rpcServiceMethodInfoMap.get(serviceId).get(rpcServiceMethodInfo.getMethodAlias()).setIsValid(false);
    }

    private void modifyServiceMethod(RpcServiceMethodInfo rpcServiceMethodInfo) {
        Long serviceId = rpcServiceMethodInfo.getServiceId();
        if (!rpcServiceMethodInfoMap.containsKey(serviceId) || rpcServiceMethodInfoMap.get(serviceId).containsKey(rpcServiceMethodInfo.getMethodAlias())) {
            logger.info("service or serviceMethod not in the cache for modify {} ", JSON.toJSONString(rpcServiceMethodInfo));
            return;
        }
        logger.info("service method update ,new service method is {} ", JSON.toJSONString(rpcServiceMethodInfo));
        rpcServiceMethodInfoMap.get(serviceId).put(rpcServiceMethodInfo.getMethodAlias(), rpcServiceMethodInfo);
    }


    private void deleteServiceMethod(RpcServiceMethodInfo rpcServiceMethodInfo) {
        Long serviceId = rpcServiceMethodInfo.getServiceId();
        if (!rpcServiceMethodInfoMap.containsKey(serviceId) || rpcServiceMethodInfoMap.get(serviceId).containsKey(rpcServiceMethodInfo.getMethodAlias())) {
            logger.info("service or serviceMethod not in the cache for delete {} ", JSON.toJSONString(rpcServiceMethodInfo));
            return;
        }
        logger.info("service method delete {} ",JSON.toJSONString(rpcServiceMethodInfo));
        rpcServiceMethodInfoMap.get(serviceId).remove(rpcServiceMethodInfo.getMethodAlias());
    }

    private void enalbeService(RpcServiceInfo rpcServiceInfo) {
        String appId = rpcServiceInfo.getAppAlias();
        if (!rpcServiceInfoMap.containsKey(appId)) {
            logger.info("appId {} not exist in the cache for enable ", JSON.toJSONString(rpcServiceInfo.getAppAlias()));
            return;
        }
        String serviceKey = String.format("%s_%s", rpcServiceInfo.getServiceAlias(), rpcServiceInfo.getServiceVersion());
        if (!rpcServiceInfoMap.get(appId).containsKey(serviceKey)) {
            logger.info("appId {} not contains  service {} version {} for enable ", rpcServiceInfo.getAppAlias(), rpcServiceInfo.getServiceAlias(), rpcServiceInfo.getServiceVersion());
            return;
        }
        logger.info("service enable {} ",JSON.toJSONString(rpcServiceInfo));
        rpcServiceInfoMap.get(appId).get(serviceKey).setIsValid(true);
    }

    private void disableService(RpcServiceInfo rpcServiceInfo) {
        String appId = rpcServiceInfo.getAppAlias();
        if (!rpcServiceInfoMap.containsKey(appId)) {
            logger.info("appId {} not exist in the cache for disable ", JSON.toJSONString(rpcServiceInfo.getAppAlias()));
            return;
        }
        String serviceKey = String.format("%s_%s", rpcServiceInfo.getServiceAlias(), rpcServiceInfo.getServiceVersion());
        if (!rpcServiceInfoMap.get(appId).containsKey(serviceKey)) {
            logger.info("appId {} not contains  service {} version {} for disable ", rpcServiceInfo.getAppAlias(), rpcServiceInfo.getServiceAlias(), rpcServiceInfo.getServiceVersion());
            return;
        }
        logger.info("service disable {} ",JSON.toJSONString(rpcServiceInfo));
        rpcServiceInfoMap.get(appId).get(serviceKey).setIsValid(false);
    }

    private void modifyService(RpcServiceInfo rpcServiceInfo) {
        String appId = rpcServiceInfo.getAppAlias();
        if (!rpcServiceInfoMap.containsKey(appId)) {
            logger.info("appId {} not exist in the cache for modify ", JSON.toJSONString(rpcServiceInfo.getAppAlias()));
            return;
        }
        String serviceKey = String.format("%s_%s", rpcServiceInfo.getServiceAlias(), rpcServiceInfo.getServiceVersion());
        if (!rpcServiceInfoMap.get(appId).containsKey(serviceKey)) {
            logger.info("appId {} not contains  service {} version {} for modify ", rpcServiceInfo.getAppAlias(), rpcServiceInfo.getServiceAlias(), rpcServiceInfo.getServiceVersion());
            return;
        }
        logger.info("service modify {} ",JSON.toJSONString(rpcServiceInfo));
        rpcServiceInfoMap.get(appId).put(serviceKey, rpcServiceInfo);
    }

    private void deleteService(RpcServiceInfo rpcServiceInfo) {
        String appId = rpcServiceInfo.getAppAlias();
        if (!rpcServiceInfoMap.containsKey(appId)) {
            logger.info("appId {} not exist in the cache for disable for  ", JSON.toJSONString(rpcServiceInfo.getAppAlias()));
            return;
        }
        String serviceKey = String.format("%s_%s", rpcServiceInfo.getServiceAlias(), rpcServiceInfo.getServiceVersion());
        if (!rpcServiceInfoMap.get(appId).containsKey(serviceKey)) {
            logger.info("appId {} not contains  service {} version {} for modify ", rpcServiceInfo.getAppAlias(), rpcServiceInfo.getServiceAlias(), rpcServiceInfo.getServiceVersion());
            return;
        }
        logger.info("service delete {} ",JSON.toJSONString(rpcServiceInfo));
        rpcServiceInfoMap.get(appId).remove(serviceKey);
    }

    /**
     * add serviceMethod
     *
     * @param rpcServiceMethodInfo
     */
    private void addServiceMethod(RpcServiceMethodInfo rpcServiceMethodInfo) {
        Long serviceId = rpcServiceMethodInfo.getServiceId();
        if (!rpcServiceMethodInfoMap.containsKey(serviceId)) {
            rpcServiceMethodInfoMap.put(serviceId, Maps.newHashMap());
        }
        logger.info("service method add {} ",JSON.toJSONString(rpcServiceMethodInfo));
        rpcServiceMethodInfoMap.get(serviceId).put(rpcServiceMethodInfo.getMethodAlias(), rpcServiceMethodInfo);
    }

    /**
     * add service
     *
     * @param rpcServiceInfo
     */
    private void addService(RpcServiceInfo rpcServiceInfo) {
        String appId = rpcServiceInfo.getAppAlias();
        if (!rpcServiceInfoMap.containsKey(appId)) {
            rpcServiceInfoMap.put(appId, Maps.newHashMap());
        }
        String serviceKey = String.format("%s_%s", rpcServiceInfo.getServiceAlias(), rpcServiceInfo.getServiceVersion());
        logger.info("service add {} ",JSON.toJSONString(rpcServiceInfo));
        rpcServiceInfoMap.get(appId).put(serviceKey, rpcServiceInfo);
    }

    public RpcServiceInfo getServiceInfo(ServiceInstance serviceInstance) {
        Map<String, RpcServiceInfo> serviceInfoMap = rpcServiceInfoMap.get(serviceInstance.getAppName());
        String serviceKey = String.format("%s_%s", serviceInstance.getServiceName(), serviceInstance.getServiceVersion());
        RpcServiceInfo rpcServiceInfo = serviceInfoMap.get(serviceKey);
        return rpcServiceInfo;
    }

    public RpcServiceMethodInfo getServiceMethodInfo(Long serviceId, ServiceInstance serviceInstance) {
        RpcServiceMethodInfo rpcServiceMethodInfo = rpcServiceMethodInfoMap.get(serviceId).get(serviceInstance.getServiceMethod());
        return rpcServiceMethodInfo;
    }

    /**
     * 当前请求的服务是否存在
     *
     * @return
     */
    public boolean isServiceValidate(ServiceInstance serviceInstance) {
        if (!rpcServiceInfoMap.containsKey(serviceInstance.getAppName())) {
            return false;
        }
        Map<String, RpcServiceInfo> serviceInfoMap = rpcServiceInfoMap.get(serviceInstance.getAppName());
        String serviceKey = String.format("%s_%s", serviceInstance.getServiceName(), serviceInstance.getServiceVersion());
        if (!serviceInfoMap.containsKey(serviceKey)) {
            return false;
        }
        RpcServiceInfo rpcServiceInfo = serviceInfoMap.get(serviceKey);
        final Long id = rpcServiceInfo.getId();
        if (!rpcServiceMethodInfoMap.containsKey(id) || !rpcServiceMethodInfoMap.get(id).containsKey(serviceInstance.getServiceMethod())) {
            return false;
        }
        return true;
    }
}

