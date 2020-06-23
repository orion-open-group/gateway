package com.orion;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.orion.mobile.gateway.domain.FieldDefinition;
import com.orion.mobile.gateway.service.ServiceParamBuildService;
import lombok.Data;
import org.apache.dubbo.common.utils.ReflectUtils;
import org.junit.Assert;

import java.util.*;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/12/12 17:40
 * @Version 1.0.0
 */
public class ParamBuildTest {
    static ServiceParamBuildService dubboServiceInvoker = new ServiceParamBuildService();

    public static void main(String[] args) throws Exception {
        JSONObject jsonObject = new JSONObject();
        JSONObject subParam = new JSONObject();
        jsonObject.put("param", subParam);

        subParam.put("list", Arrays.asList("123", 42, "89"));
        subParam.put("map", new HashMap() {{
            put("zhangsan", "two");
            put("lisi", "three");
        }});
        subParam.put("set", new HashSet() {{
            add("zh");
            add("cn");
            add("eu");
        }});
        subParam.put("stringArray", new String[]{"zhangsan", "lisi"});
        subParam.put("date", "2019-11");
        subParam.put("primaryType", 21.32);
        subParam.put("pN", "21");
        JSONObject thridDegree = new JSONObject();
        subParam.put("thridDegree", thridDegree);
        thridDegree.put("sName", "Szhangsan");
        thridDegree.put("Age", "21");

        test("param.date", "yyyy-MM", jsonObject, Date.class);
        test("param.primaryType", null, jsonObject, String.class);
        test("param.list", null, jsonObject, java.util.List.class);
        test("param.map", null, jsonObject, java.util.Map.class);
        test("param.set", null, jsonObject, java.util.Set.class);
        test("param.stringArray", null, jsonObject, String[].class);
        //test composite double attribute
        testComB(jsonObject);


    }

    private static void testComB(JSONObject jsonObject) throws Exception {
        FieldDefinition parent = new FieldDefinition();
        parent.setFieldType(ReflectUtils.getName(CompoA.class));
        List<FieldDefinition> subFieldList = Lists.newArrayList();
        parent.setSubFields(subFieldList);
        FieldDefinition fieldDefinition = new FieldDefinition();
        subFieldList.add(fieldDefinition);
        fieldDefinition.setFieldName("name");
        fieldDefinition.setRef("param.thridDegree.sName");
        fieldDefinition.setFieldType("java.lang.String");
        fieldDefinition = new FieldDefinition();
        subFieldList.add(fieldDefinition);
        fieldDefinition.setFieldName("age");
        fieldDefinition.setRef("param.thridDegree.Age");
        fieldDefinition.setFieldType("java.lang.Long");
        fieldDefinition = new FieldDefinition();
        subFieldList.add(fieldDefinition);
        fieldDefinition.setFieldName("compoB");
        fieldDefinition.setFieldType(ReflectUtils.getName(CompoB.class));
        subFieldList = Lists.newArrayList();
        fieldDefinition.setSubFields(subFieldList);
        fieldDefinition = new FieldDefinition();
        subFieldList.add(fieldDefinition);
        fieldDefinition.setFieldName("sex");
        fieldDefinition.setRef("param.thridDegree.Age");
        fieldDefinition.setFieldType("java.lang.String");
        fieldDefinition = new FieldDefinition();
        subFieldList.add(fieldDefinition);
        fieldDefinition.setFieldName("salary");
        fieldDefinition.setRef("param.pN");
        fieldDefinition.setFieldType("java.lang.Double");


        Object value = dubboServiceInvoker.buildParamValue(Arrays.asList(parent), jsonObject)[0];
        CompoA compoA = JSON.parseObject(JSON.toJSONString(value), CompoA.class);
        Assert.assertEquals(compoA.getAge(), 21);
        Assert.assertEquals(compoA.getName(), "Szhangsan");
        Assert.assertEquals(compoA.getCompoB().getSex(), "21");
        Assert.assertEquals(compoA.getCompoB().getSalary(), 21d,0);

        System.out.println(value);
    }

    private static void test(String valueRef, String format, JSONObject valueMap, Class classValidate) throws Exception {
        FieldDefinition fieldDefinition = new FieldDefinition();
        fieldDefinition.setFieldType(ReflectUtils.getName(classValidate));
        fieldDefinition.setRef(valueRef);
        fieldDefinition.setFieldFormat(format);
        Object value = dubboServiceInvoker.buildParamValue(Arrays.asList(fieldDefinition), valueMap)[0];
        String name = ReflectUtils.getName(value.getClass());
        if (!classValidate.isAssignableFrom(value.getClass())) {
//        if (!name.equalsIgnoreCase(ReflectUtils.getName(classValidate))) {
            throw new RuntimeException("validate fail " + valueRef);
        }
        System.out.println("test " + ReflectUtils.getName(classValidate) + " success ");

    }
}

@Data
class CompoA {
    String name;
    int age;
    CompoB compoB;
}

@Data
class CompoB {
    String sex;
    Double salary;
}
