package com.orion.mobile.gateway.filters.endpoint;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.filters.http.HttpSyncEndpoint;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.netflix.zuul.message.http.HttpResponseMessage;
import com.netflix.zuul.message.http.HttpResponseMessageImpl;
import com.netflix.zuul.stats.status.StatusCategoryUtils;
import com.netflix.zuul.stats.status.ZuulStatusCategory;
import com.orion.mobile.gateway.domain.GatewayResponse;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/11/1 10:47
 * @Version 1.0.0
 */
public class NoMatchEndpoint extends HttpSyncEndpoint {
    @Override
    public HttpResponseMessage apply(HttpRequestMessage request) {
        HttpResponseMessage resp = new HttpResponseMessageImpl(request.getContext(), request, 200);
        GatewayResponse gatewayResponse = new GatewayResponse();
        gatewayResponse.setCode("404");
        gatewayResponse.setMessage("not found page for " + request.getPath());
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
