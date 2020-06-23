package com.orion.mobile.gateway.domain;

import lombok.Data;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/11 9:56
 * @Version 1.0.0
 */
@Data
public class DubboConfig {
    private String application;
    private String addr;
    private String protocol;
    private int timeout;
    private int retry;
}
