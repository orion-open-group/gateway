package com.orion.mobile.gateway.filters.endpoint;

import com.google.inject.Singleton;
import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import org.apache.commons.lang3.StringUtils;

@Singleton
public class HealthCheckEndpoint extends HttpSyncEndpoint {
    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        HttpResponseMessage resp = new HttpResponseMessageImpl(request.getContext(), request, 200);
        resp.setBodyAsText("healthy");
        StatusCategoryUtils.setStatusCategory(request.getContext(), ZuulStatusCategory.SUCCESS);
        return resp;
    }

    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        String path = StringUtils.trim(StringUtils.removeStart(msg.getPath(), "/"));
        if (StringUtils.equalsAnyIgnoreCase("health", path)) {
            return true;
        }
        return false;
    }
}
