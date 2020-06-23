package com.orion.mobile.gateway.domain;

import lombok.Data;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/31 19:29
 * @Version 1.0.0
 */
@Data
public class ServiceInstance {
    String serviceName;
//    String appId;
    String appName;
    String serviceMethod;
    String serviceVersion;

    public ServiceInstance(String serviceName, String appName, String serviceMethod, String serviceVersion) {
        this.serviceName = serviceName;
        this.appName = appName;
        this.serviceMethod = serviceMethod;
        this.serviceVersion = serviceVersion;
    }

}
