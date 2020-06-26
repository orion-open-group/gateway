package com.orion.mobile.gateway.util;

import java.util.UUID;

/**
 * @Description TODO
 * @Author beedoorwei
 * @Date 2020/6/27 7:20
 * @Version 1.0.0
 */
public class UUIDUtils {
    public static String next() {
        return UUID.randomUUID().toString();
    }
}
