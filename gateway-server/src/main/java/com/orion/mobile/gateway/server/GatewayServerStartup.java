package com.orion.mobile.gateway.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.config.DynamicIntProperty;
import com.netflix.discovery.EurekaClient;
import com.netflix.netty.common.accesslog.AccessLogPublisher;
import com.netflix.netty.common.channel.config.ChannelConfig;
import com.netflix.netty.common.channel.config.CommonChannelConfigKeys;
import com.netflix.netty.common.metrics.EventLoopGroupMetrics;
import com.netflix.netty.common.proxyprotocol.StripUntrustedProxyHeadersHandler;
import com.netflix.netty.common.status.ServerStatusManager;
import com.netflix.spectator.api.Registry;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.FilterUsageNotifier;
import com.netflix.zuul.RequestCompleteHandler;
import com.netflix.zuul.context.SessionContextDecorator;
import com.netflix.zuul.netty.server.BaseServerStartup;
import com.netflix.zuul.netty.server.DirectMemoryMonitor;
import com.netflix.zuul.netty.server.ZuulServerChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.group.ChannelGroup;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/30 20:07
 * @Version 1.0.0
 */
@Singleton
public class GatewayServerStartup extends BaseServerStartup {


    @Inject
    public GatewayServerStartup(ServerStatusManager serverStatusManager, FilterLoader filterLoader, SessionContextDecorator sessionCtxDecorator, FilterUsageNotifier usageNotifier, RequestCompleteHandler reqCompleteHandler, Registry registry, DirectMemoryMonitor directMemoryMonitor, EventLoopGroupMetrics eventLoopGroupMetrics, EurekaClient discoveryClient, ApplicationInfoManager applicationInfoManager, AccessLogPublisher accessLogPublisher) {
        super(serverStatusManager, filterLoader, sessionCtxDecorator, usageNotifier, reqCompleteHandler, registry, directMemoryMonitor, eventLoopGroupMetrics, discoveryClient, applicationInfoManager, accessLogPublisher);
    }

    @Override
    protected Map<Integer, ChannelInitializer> choosePortsAndChannels(ChannelGroup clientChannels, ChannelConfig channelDependencies) {
        Map<Integer, ChannelInitializer<?>> addrsToChannels = new HashMap<>();

        int port = new DynamicIntProperty("zuul.server.port.main", 7001).get();

        ChannelConfig channelConfig = defaultChannelConfig();

        channelConfig.set(CommonChannelConfigKeys.allowProxyHeadersWhen, StripUntrustedProxyHeadersHandler.AllowWhen.ALWAYS);
        channelConfig.set(CommonChannelConfigKeys.preferProxyProtocolForClientIp, false);
        channelConfig.set(CommonChannelConfigKeys.isSSlFromIntermediary, false);
        channelConfig.set(CommonChannelConfigKeys.withProxyProtocol, false);

        addrsToChannels.put(port,
                new ZuulServerChannelInitializer(
                        port, channelConfig, channelDependencies, clientChannels));

        return Collections.unmodifiableMap(addrsToChannels);
    }

}
