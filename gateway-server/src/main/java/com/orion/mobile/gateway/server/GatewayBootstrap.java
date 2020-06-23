package com.orion.mobile.gateway.server;

import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.netflix.governator.InjectorBuilder;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.netty.server.BaseServerStartup;
import com.netflix.zuul.netty.server.Server;
import com.orion.logger.BusinessLoggerFactory;
import com.orion.mobile.gateway.module.GatewayModule;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Version 1.0.0
 */
@Singleton
public class GatewayBootstrap {

    private Logger logger = BusinessLoggerFactory.getBusinessLogger("GATEWAY", GatewayBootstrap.class);

    public Server buildServer() throws Exception {
        long startTime = System.currentTimeMillis();
        init();
        Injector injector = InjectorBuilder.fromModule(new GatewayModule()).createInjector();
        BaseServerStartup serverStartup = injector.getInstance(BaseServerStartup.class);
        Server server = serverStartup.server();
        logger.info("gateway zuul init use time {} ", System.currentTimeMillis() - startTime);
        addShutdownHocker(server);
        return server;
    }

    /**
     * because the guava cann't find the class by the package
     * @throws Exception
     */
    public void init() throws Exception {
        List<String> decodeProcessorList = load("classpath*:com/orion/mobile/gateway/filters/**/*.class", ZuulFilter.class);
        if (CollectionUtils.isNotEmpty(decodeProcessorList)) {
            logger.info("all filter class {} ", StringUtils.join(decodeProcessorList, ","));
            ConfigurationManager.getConfigInstance().setProperty("zuul.filters.classes", StringUtils.join(decodeProcessorList, ","));
        }
    }

    private List<String> load(String path, Class filter) throws Exception {
        ResourcePatternResolver resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(null);
        CachingMetadataReaderFactory cachingMetadataReaderFactory = new CachingMetadataReaderFactory();
        List<String> targetList = Lists.newArrayList();
        Resource[] resources = resourcePatternResolver.getResources(path);
        if (resources != null && resources.length > 0) {
            for (int i = 0; i < resources.length; i++) {
                MetadataReader metadataReader = cachingMetadataReaderFactory.getMetadataReader(resources[i]);
                if (!metadataReader.getClassMetadata().isConcrete()) {
                    continue;
                }
                String className = metadataReader.getClassMetadata().getClassName();
                Class aClass = Class.forName(className);
                if (filter.isAssignableFrom(aClass)) {
                    targetList.add(className);
                }
            }
        }
        return targetList;
    }

    private void addShutdownHocker(Server server) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
                TimeUnit.SECONDS.sleep(2);
            } catch (Throwable e) {
                logger.error("logger error");
            }
        }
        ));
    }
}
