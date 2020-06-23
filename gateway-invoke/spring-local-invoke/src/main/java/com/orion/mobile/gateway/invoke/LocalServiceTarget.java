package com.orion.mobile.gateway.invoke;

import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/13 15:10
 * @Version 1.0.0
 */
@Data
public class LocalServiceTarget {
    Method method;
    Object service;
    Type[] paramTypes;
}
