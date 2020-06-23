package com.orion.mobile.gateway.util;

import lombok.Data;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/24 11:01
 * @Version 1.0.0
 */
@Data
public class LocalParam {
    String token;
    String traceId;

    public static LocalParam of(String token, String traceId) {
        LocalParam localParam = new LocalParam();
        localParam.setToken(token);
        localParam.setTraceId(traceId);
        return localParam;
    }
}
