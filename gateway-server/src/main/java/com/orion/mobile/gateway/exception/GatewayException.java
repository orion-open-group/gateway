package com.orion.mobile.gateway.exception;

import com.netflix.zuul.exception.ZuulException;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/30 22:47
 * @Version 1.0.0
 */
public class GatewayException extends ZuulException {

    public GatewayException(int code, String msg) {
        super(msg);
        setStatusCode(code);
    }
}
