package com.orion.mobile.gateway.domain;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/11 9:20
 * @Version 1.0.0
 */
@Data
@ConfigurationProperties(prefix="mgateway")
public class MGatewayConfig {
    private String gatewayPort;
    private boolean printStaic;
    private DubboConfig dubbo;
    private DatabaseConfig db;
}
