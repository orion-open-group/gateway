package com.orion.mobile.gateway.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.orion.mobile.gateway.domain.FieldDefinition;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;
import com.orion.mobile.gateway.util.DataTypeTranslator;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class AppTest {
    /**
     * Rigorous Test :-)
     */
    public static void main(String[] args) throws Exception {

//        Method[] methods = AppTest.class.getMethods();
//        for (Method method : methods) {
//            System.out.println(method.getName());
//            Class<?>[] parameterTypes = method.getParameterTypes();
//            for (Class<?> parameterType : parameterTypes) {
//                System.out.println(ReflectUtils.getName(parameterType));
//            }
//            System.out.println("###");
//        }
//
//        JSONArray v = JSONArray.parseArray("[\n" +
//                "    {\n" +
//                "        \"fieldName\": \"bean\",\n" +
//                "        \"fieldType\": \"com.orion.dto.LoginParamBean\",\n" +
//                "        \"subFields\": [\n" +
//                "            {\n" +
//                "                \"fieldType\": \"java.lang.String\",\n" +
//                "\t\t\t\t\t\t\t\t\"ref\":\"phoneNo\"\n" +
//                "            },\n" +
//                "            {\n" +
//                "                \"fieldType\": \"java.lang.String\",\n" +
//                "                \"ref\":\"bean.password\"\n" +
//                "            }\n" +
//                "        ]\n" +
//                "    }\n" +
//                "]");
//        System.out.println(v);
//        AppTest appTest = new AppTest();
//        RpcServiceMethodInfo rpcServiceMethodInfo = new RpcServiceMethodInfo();
//        List<FieldDefinition> fieldDefinitionList = new ArrayList<>();
//        rpcServiceMethodInfo.setParamFieldList(fieldDefinitionList);
//        FieldDefinition fieldDefinition = new FieldDefinition();
//        fieldDefinition.setFieldName("s");
//        fieldDefinition.setFieldType("com.filedBack.xx");
//        fieldDefinitionList.add(fieldDefinition);
//        String[] strings = new String[3];
//        fieldDefinition.setFieldType(strings.getClass().getTypeName());
//        FieldDefinition sub1= new FieldDefinition();
//        sub1.setFieldType(String.class.getTypeName());
//        sub1.setRef("su.c4");
//        fieldDefinition.getSubFields().add(sub1);
//        sub1= new FieldDefinition();
//        sub1.setFieldType(String.class.getTypeName());
//        sub1.setRef("age");
//        fieldDefinition.getSubFields().add(sub1);
//
//        fieldDefinition = new FieldDefinition();
//        fieldDefinition.setFieldName("s");
//        fieldDefinition.setFieldType("com.orion.1");
//        fieldDefinitionList.add(fieldDefinition);
//        sub1= new FieldDefinition();
//        sub1.setFieldType(Date.class.getTypeName());
//        sub1.setFieldFormat("yyyy-MM-dd");
//        sub1.setFieldName("dateP");
//        sub1.setRef("c1");
//        fieldDefinition.getSubFields().add(sub1);
//        sub1= new FieldDefinition();
//        sub1.setFieldType(Date.class.getTypeName());
//        sub1.setFieldName("timeP");
//        sub1.setFieldFormat("HH:mm:ss");
//        sub1.setRef("su.c2");
//        fieldDefinition.getSubFields().add(sub1);
//
//
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("name", "zhangsan");
//        jsonObject.put("age", 21);
//        jsonObject.put("c1", "2011-10-21");
//        jsonObject.put("c3", "wangwu");
//
//        JSONObject jsonObject1 = new JSONObject();
//        jsonObject1.put("c2", "10:10:00");
//        jsonObject1.put("c4", 32);
//        jsonObject.put("su", jsonObject1);
//
//        Object[] objects = appTest.buildParamValue(rpcServiceMethodInfo, jsonObject);
//        System.out.println(JSON.toJSONString(objects));


        T1 t1 = new T1();
        t1.setA("a");
        t1.setB("2");
        System.out.println(JSON.toJSONString(t1));
    }

    public static String iv(String s, Double [] d, List<AppTest> list, Map<String,List<AppTest>> map, Set<AppTest> set,int [] c)
    {
        return "";
    }


    private Object[] buildParamValue(RpcServiceMethodInfo rpcServiceMethodInfo, JSONObject messageBody) throws Exception {
        List<Object> result = Lists.newArrayList();
        for (FieldDefinition fieldDefinition : rpcServiceMethodInfo.getParamFieldList()) {
            Object v = getValue(fieldDefinition, messageBody);
            result.add(v);
        }

        return result.toArray();
    }


    private Object getValue(FieldDefinition fieldDefinition, JSONObject messageBody) throws Exception {
        List<FieldDefinition> subFields = fieldDefinition.getSubFields();
        String fieldType = fieldDefinition.getFieldType();
        if (DataTypeTranslator.isDate(fieldType)) {
            String valueRefExpress = fieldDefinition.getRef();
            String[] split = StringUtils.split(valueRefExpress, ".");
            Date dateValue = getDateValue(fieldDefinition.getFieldFormat(), split, messageBody);
            return dateValue;
        } else if (DataTypeTranslator.isPrimitive(fieldType)) {
            String valueRefExpress = fieldDefinition.getRef();
            String[] split = StringUtils.split(valueRefExpress, ".");
            Object value = getTargetValue(fieldDefinition.getFieldType(), split, messageBody);
            return value;
        } else if (DataTypeTranslator.isArray(fieldType)) {
            List<Object> valueList = Lists.newArrayList();
            for (FieldDefinition subField : subFields) {
                Object value = getValue(subField, messageBody);
                valueList.add(value);
            }
            return valueList;
        } else if (DataTypeTranslator.isCollection(fieldType) || CollectionUtils.isNotEmpty(fieldDefinition.getSubFields())) {
            Map<String, Object> valueMap = Maps.newHashMap();
            for (FieldDefinition subField : subFields) {
                Object value = getValue(subField, messageBody);
                valueMap.put(subField.getFieldName(), value);
            }
            return valueMap;
        } else {
            String valueRefExpress = fieldDefinition.getRef();
            String[] split = StringUtils.split(valueRefExpress, ".");
            String value = getTargetValue(split, messageBody);
            if(value != null)
            {
                return JSON.parseObject(value,Class.forName(fieldType));
            }
        }
        return null;
    }

    private String getTargetValue(String[] split, JSONObject messageBody) {
        String valueStr = getValueStr(split, 0, messageBody);
        return valueStr;
    }

    private Object getTargetValue(String fieldType, String[] split, JSONObject messageBody) {
        String valueStr = getValueStr(split, 0, messageBody);
        return DataTypeTranslator.getStringCastToPrimitiveFunctions().get(fieldType).apply(valueStr);
    }

    private Date getDateValue(String format, String[] split, JSONObject messageBody) throws Exception {
        String valueStr = getValueStr(split, 0, messageBody);
        if (StringUtils.isBlank(format)) {
            throw new RuntimeException("data time must have expression");
        }
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date parse = dateFormat.parse(valueStr);
        return parse;
    }

    private String getValueStr(String[] split, int i, JSONObject messageBody) {
        if (split.length == 0) {
            return null;
        }
        if (split.length == i + 1) {
            return messageBody.getString(split[i]);
        }
        Object o = messageBody.get(split[i]);
        if (o instanceof Map) {
            String valueStr = getValueStr(split, i + 1, (JSONObject) o);
            return valueStr;
        }
        throw new RuntimeException("data type not right ");
    }
}
@Data
class T1{
    String a;
    String b;
}
