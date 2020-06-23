package com.orion.mobile.gateway.filters.endpoint;

import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/11/1 20:47
 * @Version 1.0.0
 */
public class OperationEndPoint extends HttpSyncEndpoint {
    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        HttpResponseMessage response = new HttpResponseMessageImpl(request.getContext(), request, 200);
        response.getHeaders().set("Access-Control-Allow-Origin", "*");
        response.getHeaders().set("Access-Control-Allow-Credentials", "true");
        response.getHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.getHeaders().set("Access-Control-Allow-Headers", "authorization, content-type");
        response.getHeaders().set("Access-Control-Expose-Headers", "X-forwared-port, X-forwarded-host");
        response.getHeaders().set("Vary", "Origin,Access-Control-Request-Method,Access-Control-Request-Headers");
        response.setBodyAsText("success");
        StatusCategoryUtils.setStatusCategory(request.getContext(), ZuulStatusCategory.SUCCESS);
        return response;
    }

    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        return false;
    }
}
