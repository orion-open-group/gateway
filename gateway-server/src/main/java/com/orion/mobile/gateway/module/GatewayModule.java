package com.orion.mobile.gateway.module;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.netty.common.accesslog.AccessLogPublisher;
import com.netflix.netty.common.status.ServerStatusManager;
import com.netflix.spectator.api.DefaultRegistry;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.BasicRequestCompleteHandler;
import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;
import com.netflix.zuul.context.ZuulSessionContextDecorator;
import com.netflix.zuul.init.ZuulFiltersModule;
import com.netflix.zuul.netty.server.BaseServerStartup;
import com.netflix.zuul.netty.server.ClientRequestReceiver;
import com.netflix.zuul.origins.BasicNettyOriginManager;
import com.netflix.zuul.origins.OriginManager;
import com.netflix.zuul.stats.BasicRequestMetricsPublisher;
import com.netflix.zuul.stats.RequestMetricsPublisher;
import com.orion.mobile.gateway.repository.DatabaseServiceLoader;
import com.orion.mobile.gateway.repository.ServiceLoader;
import com.orion.mobile.gateway.server.GatewayServerStartup;
import com.orion.mobile.gateway.util.GatewayEncrypt;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/30 20:07
 * @Version 1.0.0
 */
@Singleton
public class GatewayModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(BaseServerStartup.class).to(GatewayServerStartup.class);

        // use provided basic netty origin manager
        bind(OriginManager.class).to(BasicNettyOriginManager.class);

        bind(ServiceLoader.class).to(DatabaseServiceLoader.class);

        //bind(ServiceDefineCacheService.class);
        // zuul filter loading
        install(new ZuulFiltersModule());
        install(new InvokerModule());
        bind(FilterFileManager.class).asEagerSingleton();

        // health/discovery status
        bind(ServerStatusManager.class);
        // decorate new sessions when requests come in
        bind(SessionContextDecorator.class).to(ZuulSessionContextDecorator.class);
        // atlas metrics registry
        bind(Registry.class).to(DefaultRegistry.class);
        // metrics post-request completion
        bind(RequestCompleteHandler.class).to(BasicRequestCompleteHandler.class);
        // discovery client
        bind(AbstractDiscoveryClientOptionalArgs.class).to(DiscoveryClient.DiscoveryClientOptionalArgs.class);
        // timings publisher
        bind(RequestMetricsPublisher.class).to(BasicRequestMetricsPublisher.class);

        // access logger, including request ID generator
        bind(AccessLogPublisher.class).toInstance(new AccessLogPublisher("ACCESS",
                (channel, httpRequest) -> ClientRequestReceiver.getRequestFromChannel(channel).getContext().getUUID()));

    }

    @Provides
    public DruidDataSource druidDataSource() throws Exception {
        DruidDataSource druidDataSource = new DruidDataSource();
        final AbstractConfiguration configInstance = ConfigurationManager.getConfigInstance();
        String token = configInstance.getString("datasource.token");
        if (StringUtils.isNotEmpty(token)) {
            String decrypt = GatewayEncrypt.decrypt(token);
            JSONObject jsonObject = JSON.parseObject(decrypt);
            druidDataSource.setUrl(jsonObject.getString("url"));
            druidDataSource.setUsername(jsonObject.getString("user"));
            druidDataSource.setPassword(jsonObject.getString("pwd"));
        } else {
            druidDataSource.setUsername(GatewayEncrypt.decrypt(configInstance.getString("datasource.username")));
            druidDataSource.setPassword(GatewayEncrypt.decrypt(configInstance.getString("datasource.password")));
            druidDataSource.setUrl(GatewayEncrypt.decrypt(configInstance.getString("datasource.url")));
        }
        druidDataSource.setMaxActive(configInstance.getInteger("datasource.active", 2));
        druidDataSource.setDefaultAutoCommit(false);
        druidDataSource.init();
        return druidDataSource;
    }

}
