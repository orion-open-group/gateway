package com.orion.mobile.gateway.util;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/11/2 15:35
 * @Version 1.0.0
 */
public class InvokeParamContext {

    static ThreadLocal<LocalParam> userThreadLocal = new ThreadLocal<LocalParam>();

    public static void set(LocalParam user) {
        userThreadLocal.set(user);
    }

    public static void remove() {
        userThreadLocal.remove();
    }

    public static LocalParam get() {
        return userThreadLocal.get();
    }
}
