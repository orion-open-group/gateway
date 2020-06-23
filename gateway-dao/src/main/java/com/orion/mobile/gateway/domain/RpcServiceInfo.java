package com.orion.mobile.gateway.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class RpcServiceInfo implements Serializable {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 服务别名，保持唯一性
     */
    private String serviceAlias;

    /**
     * 服务类型，目前只有DUBBO，后续可增加RMB
     */
    private String serviceType;

    /**
     * 服务版本号，外部调用时需带入
     */
    private String serviceVersion;

    /**
     * 服务配置
     */
    private String serviceConfig;

    /**
     * 服务描述
     */
    private String description;

    /**
     * 所属应用ID
     */
    private String appId;

    /**
     * 所属应用名称
     */
    private String appAlias;

    /**
     * 是否有效
     */
    private Boolean isValid;

    /**
     * 是否删除
     */
    private Boolean isDeleted;

    private Long  timestamp;

    private Map<String, Object> serviceConfigMap;

    public <T> T getConfigValue(String key) {
        if (serviceConfigMap.containsKey(key)) {
            return (T) serviceConfigMap.get(key);
        }
        return null;
    }
}
