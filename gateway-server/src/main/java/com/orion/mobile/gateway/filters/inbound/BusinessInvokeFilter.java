package com.orion.mobile.gateway.filters.inbound;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.Singleton;
import com.netflix.zuul.context.SessionContext;
import com.netflix.zuul.filters.http.HttpInboundSyncFilter;
import com.netflix.zuul.message.http.HttpRequestMessage;
import org.slf4j.LoggerFactory;
import com.orion.mobile.gateway.exception.GatewayException;
import com.orion.mobile.gateway.util.GatewayConstants;
import io.netty.handler.codec.http.HttpMethod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;


/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/30 22:37
 * @Version 1.0.0
 */
@Singleton
public class BusinessInvokeFilter extends HttpInboundSyncFilter {
    private Logger logger = LoggerFactory.getLogger(BusinessInvokeFilter.class);

    @Override
    public int filterOrder() {
        return 0;
    }


    @Override
    public boolean shouldFilter(HttpRequestMessage msg) {
        if (msg.getMethod().equalsIgnoreCase(HttpMethod.OPTIONS.name())) {
            return false;
        }
        String path = StringUtils.trim(StringUtils.removeStart(msg.getPath(), "/"));
        if (StringUtils.equalsAnyIgnoreCase("rpc", path)) {
            return true;
        }
        return true;
    }

    @Override
    public boolean needsBodyBuffered(HttpRequestMessage input) {
        if (input.hasBody()) {
            return true;
        }
        return super.needsBodyBuffered(input);
    }

    @Override
    public HttpRequestMessage apply(HttpRequestMessage input) {
        SessionContext context = input.getContext();
        String bodyAsText = input.getBodyAsText();
        if (logger.isDebugEnabled()) {
            logger.debug("receive request {} ", bodyAsText);
        }
        JSONObject json = null;
        if (!JSON.isValidObject(bodyAsText)) {
            throw new GatewayException(400,
                    "invalid message format, request body is not a valid json. please check the manual... " + bodyAsText);
        }
        json = JSON.parseObject(bodyAsText);
        if (!json.containsKey(GatewayConstants.MESSAGE_HEADER)) {
            throw new GatewayException(400, "invalid message format, message header is not set. please check the manual..." + bodyAsText);
        }

        if (!json.containsKey(GatewayConstants.MESSAGE_BODY)) {
            throw new GatewayException(400, "invalid message format, message body is not set. please check the manual..." + bodyAsText);
        }

        Object header = json.get(GatewayConstants.MESSAGE_HEADER);
        if (!(header instanceof JSONObject)) {
            throw new GatewayException(400, "invalid message format, message header is not valid, please check the manual..." + bodyAsText);
        }
        JSONObject msgHeader = (JSONObject) header;

        Object body = json.get(GatewayConstants.MESSAGE_BODY);
        if (!(body instanceof JSONObject)) {
            throw new GatewayException(400, "invalid message format, message body is not valid, please check the manual..." + bodyAsText);
        }
        JSONObject msgBody = (JSONObject) body;

        String id = msgHeader.getString(GatewayConstants.MESSAGE_ID);
        if (StringUtils.isBlank(id)) {
            throw new GatewayException(400, "invalid message format, message id is blank, please check the manual..." + bodyAsText);
        }
        String action = msgHeader.getString(GatewayConstants.MESSAGE_ACTION);
        if (StringUtils.isBlank(action)) {
            throw new GatewayException(400, "action is not set for request: " + bodyAsText);
        }
        // 普通调用请求
        String accessToken = msgHeader.getString(GatewayConstants.MESSAGES_TOKEN);
        if (StringUtils.isBlank(accessToken)) {
            throw new GatewayException(400, "invalid rpc request: " + id + ", access token is not set... " + bodyAsText);
        }
        String serviceVersion = msgHeader.getString(GatewayConstants.MESSAGE_VERSION);
        if (StringUtils.isBlank(serviceVersion)) {
            throw new GatewayException(400, " rpc request: " + id + ", service version is not set... " + bodyAsText);
        }
        String[] split = StringUtils.split(action, ".");
        if (split.length == 3) {
            context.put(GatewayConstants.SERVICE_DOMAIN, split[0]);
            context.put(GatewayConstants.SERVICE_NAME, split[1]);
            context.put(GatewayConstants.SERVICE_METHOD, split[2]);
        } else {
            throw new GatewayException(400, "action config not validate ... " + bodyAsText);
        }
        String uuid = msgHeader.getString(GatewayConstants.MESSAGE_DEVICE);
        if (StringUtils.isEmpty(uuid)) {
            uuid = "NONE";
        }

        context.put(GatewayConstants.MESSAGE_ID, id);
        context.put(GatewayConstants.MESSAGE_HEADER, msgHeader);
        context.put(GatewayConstants.MESSAGE_BODY, msgBody);
        context.put(GatewayConstants.MESSAGE_VERSION, serviceVersion);
        context.put(GatewayConstants.MESSAGE_ACTION, action);
        context.put(GatewayConstants.MESSAGE_DEVICE, uuid);
        context.put(GatewayConstants.MESSAGES_TOKEN, accessToken);
        return input;
    }

}
