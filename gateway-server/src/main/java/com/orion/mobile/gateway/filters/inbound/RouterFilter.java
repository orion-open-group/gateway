package com.orion.mobile.gateway.filters.inbound;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.filters.FilterError;
import com.netflix.zuul.filters.FilterType;
import com.netflix.zuul.filters.ZuulFilter;
import com.netflix.zuul.filters.http.HttpInboundSyncFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import com.orion.logger.BusinessLoggerFactory;
import com.orion.mobile.gateway.filters.endpoint.ExceptionEndpoint;
import com.orion.mobile.gateway.filters.endpoint.NoMatchEndpoint;
import com.orion.mobile.gateway.filters.endpoint.OperationEndPoint;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;

import java.util.List;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/30 22:37
 * @Version 1.0.0
 */
@Singleton
public class RouterFilter extends HttpInboundSyncFilter {

    private Logger logger = BusinessLoggerFactory.getBusinessLogger("GATEWAY", RouterFilter.class);

    @Override
    public int filterOrder() {
        return 1;
    }

    @Inject
    FilterLoader filterLoader;

    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        return true;
    }


    @Override
    public HttpRequestMessage apply(HttpRequestMessage input) {
        //operation
        if (input.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            input.getContext().setEndpoint(OperationEndPoint.class.getTypeName());
            return input;
        }
        List<FilterError> filterErrors = input.getContext().getFilterErrors();
        if (CollectionUtils.isNotEmpty(filterErrors)) {
            input.getContext().setEndpoint(ExceptionEndpoint.class.getTypeName());
            return input;
        }
        List<ZuulFilter> filtersByTypeList = filterLoader.getFiltersByType(FilterType.ENDPOINT);
        if (CollectionUtils.isNotEmpty(filtersByTypeList)) {
            ZuulFilter endPoint = null;
            for (ZuulFilter zuulFilter : filtersByTypeList) {
                boolean b = zuulFilter.shouldFilter(input);
                if (b) {
                    endPoint = zuulFilter;
                    break;
                }
            }
            if (endPoint != null) {
                input.getContext().setEndpoint(endPoint.filterName());
            } else {
                input.getContext().setEndpoint(NoMatchEndpoint.class.getTypeName());
            }
        }
        return input;
    }
}
