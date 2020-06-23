package com.orion.mobile.gateway;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import com.orion.logger.BusinessLoggerFactory;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/30 19:58
 * @Version 1.0.0
 */
@EnableEncryptableProperties
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass = true)
@ComponentScan(basePackages = "com.orion")
public class MobileGatewayStarter {
    private static Logger logger = BusinessLoggerFactory.getBusinessLogger(MobileGatewayStarter.class);

    public static void main(String[] args) throws Exception {
        logger.info("server starter");
        SpringApplication.run(MobileGatewayStarter.class, args);
    }
}
