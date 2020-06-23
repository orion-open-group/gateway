package com.orion.mobile.gateway.service;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/13 17:21
 * @Version 1.0.0
 */
@Data
public class BeanParam {
    String name;
    Map<String, SubP> subP;
    List<SubP> subPList;
    Set<SubP> set;
    int age;
}
