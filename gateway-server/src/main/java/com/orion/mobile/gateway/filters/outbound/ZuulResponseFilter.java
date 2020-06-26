package com.orion.mobile.gateway.filters.outbound;

import com.google.inject.Singleton;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpOutboundSyncFilter;
import com.netflix.zuul.message.http.HttpResponseMessage;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@Singleton
public class ZuulResponseFilter extends HttpOutboundSyncFilter {
    private static final Logger log = LoggerFactory.getLogger(ZuulResponseFilter.class);

    @Override
    public int filterOrder() {
        return 999;
    }

    @Override
    public boolean shouldFilter(HttpResponseMessage request) {
        return true;
    }

    @Override
    public HttpResponseMessage apply(HttpResponseMessage response) {
        SessionContext context = response.getContext();
        response.getHeaders().set("Access-Control-Allow-Origin", "*");
        response.getHeaders().set("Content-Type", "application/json;charset=UTF-8");
        return response;
    }
}
