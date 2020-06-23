package com.orion.mobile.gateway.autoconfig;

import com.netflix.config.ConfigurationManager;
import com.orion.mobile.gateway.domain.MGatewayConfig;
import com.orion.logger.BusinessLoggerFactory;
import com.orion.mobile.gateway.server.GatewayBootstrap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/11 9:17
 * @Version 1.0.0
 */
@Configuration
@EnableConfigurationProperties(MGatewayConfig.class)
public class MGatwayAutoconfig {

    private Logger logger = BusinessLoggerFactory.getBusinessLogger(MGatwayAutoconfig.class);
    @Autowired
    private MGatewayConfig mGatewayConfig;

    @Bean
    public void initAndStartGateway() throws Exception {
        Properties properties = buildProperties();
        ConfigurationManager.loadProperties(properties);
        logger.warn("mgateway port :{} ", ConfigurationManager.getConfigInstance().getString("zuul.server.port.main"));
        GatewayBootstrap gatewayBootstrap = new GatewayBootstrap();
        gatewayBootstrap.buildServer().start(true);
    }

    /**
     * build properties
     *
     * @return
     */
    private Properties buildProperties() {
        Properties properties = new Properties();
        properties.put("region", "ap-mgateway-1");
        properties.put("eureka.registration.enabled", false);
        properties.put("zuul.server.port.main", mGatewayConfig.getGatewayPort());
        properties.put("eureka.shouldFetchRegistry", false);
        properties.put("eureka.validateInstanceId", false);
        properties.put("region", "ap-mgateway-1");
        properties.put("message.timeout", "100");
        properties.put("printStatic", mGatewayConfig.isPrintStaic());
        if (mGatewayConfig.getDb() != null) {
            properties.put("datasource.token", mGatewayConfig.getDb().getToken());
            properties.put("datasource.active", mGatewayConfig.getDb().getActiveNum());
        }
        if (mGatewayConfig.getDubbo() != null) {
            properties.put("dubbo.register.addr", mGatewayConfig.getDubbo().getAddr());
            properties.put("dubbo.application", mGatewayConfig.getDubbo().getApplication());
            properties.put("dubbo.register.protocol", mGatewayConfig.getDubbo().getProtocol());
            properties.put("dubbo.retry", mGatewayConfig.getDubbo().getRetry());
            properties.put("dubbo.timeout", mGatewayConfig.getDubbo().getTimeout());
        }
        return properties;
    }
}
