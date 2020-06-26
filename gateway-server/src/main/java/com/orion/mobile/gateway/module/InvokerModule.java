package com.orion.mobile.gateway.module;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.slf4j.LoggerFactory;
import com.orion.mobile.gateway.invoke.ServiceInvoker;
import com.orion.mobile.gateway.service.ServiceParamBuildService;
import org.slf4j.Logger;

import java.util.ServiceLoader;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/13 12:53
 * @Version 1.0.0
 */
public class InvokerModule extends AbstractModule {
    private static Logger logger = LoggerFactory.getLogger(InvokerModule.class);

    @Override
    protected void configure() {
        logger.info("load invoke starter");
        bind(ServiceParamBuildService.class);
        ServiceLoader<ServiceInvoker> loader = ServiceLoader.load(ServiceInvoker.class);
        for (ServiceInvoker serviceInvoker : loader) {
            bind(ServiceInvoker.class).annotatedWith(Names.named(serviceInvoker.code())).to(serviceInvoker.getClass());
        }
    }
}
