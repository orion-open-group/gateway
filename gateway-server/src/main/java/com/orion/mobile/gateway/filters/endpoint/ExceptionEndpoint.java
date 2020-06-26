package com.orion.mobile.gateway.filters.endpoint;

import com.alibaba.fastjson.JSON;
import com.google.inject.Singleton;
import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import org.slf4j.LoggerFactory;
import com.orion.mobile.gateway.domain.GatewayResponse;
import org.slf4j.Logger;

@Singleton
public class ExceptionEndpoint extends HttpSyncEndpoint {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionEndpoint.class);

    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        HttpResponseMessage resp = new HttpResponseMessageImpl(request.getContext(), request, 200);
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setCode("500");
        gatewayResponse.setMessage("inner Exception" + request.getPath());
        gatewayResponse.setSuccess(false);
        resp.setBodyAsText(JSON.toJSONString(gatewayResponse, false));
        StatusCategoryUtils.setStatusCategory(request.getContext(), ZuulStatusCategory.SUCCESS);
        return resp;
    }

    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        return false;
    }
}
