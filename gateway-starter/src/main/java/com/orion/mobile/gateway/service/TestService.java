package com.orion.mobile.gateway.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.orion.logger.BusinessLoggerFactory;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/13 17:21
 * @Version 1.0.0
 */
@Component
public class TestService {

    private static Logger logger = BusinessLoggerFactory.getBusinessLogger(TestService.class);

    public Map testMap(int age, Map<String, SubP> subP) {
        logger.info("testMap {} {}  {}", age, subP);
        SubP subp2 = subP.get("age");
        return Maps.newHashMap();
    }

    public Map testList(int age, List<SubP> subP) {
        logger.info("testList {} {}  {}", age, subP);
        return Maps.newHashMap();
    }

    public Map testSet(int age, Set<SubP> subP) {
        logger.info("testSet {} {}  {}", age, subP);
        return Maps.newHashMap();
    }

    public Map testComposite(int age, BeanParam beanParam) {
        logger.info("testComposite {} {}  {}", age, beanParam);
        return Maps.newHashMap();
    }

    public static void main(String[] args) {
        Map<String, Object> param = Maps.newHashMap();
        Map<String, Object> st = Maps.newHashMap();
        st.put("age", param);
        param.put("id", 32);
        param.put("age", 22);
        Method testMap = MethodUtils.getMatchingMethod(TestService.class, "testComposite", new Class[]{Integer.class, BeanParam.class});
        Type[] genericParameterTypes = testMap.getGenericParameterTypes();
        String s = "{\"set\":[{\"id\":211,\"age\":222}],\"subP\":{\"age\":210},\"name\":\"210\",\"subPList\":[{\"id\":223,\"age\":\"224\"}],\"age\":32}";
        Object o = JSON.parseObject(s, genericParameterTypes[1]);
        System.out.println(testMap);
    }
}
