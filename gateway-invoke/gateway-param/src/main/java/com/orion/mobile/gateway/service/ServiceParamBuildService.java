package com.orion.mobile.gateway.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.slf4j.LoggerFactory;
import com.orion.mobile.gateway.domain.FieldDefinition;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;
import com.orion.mobile.gateway.util.DataTypeTranslator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/31 20:53
 * @Version 1.0.0
 */
public class ServiceParamBuildService {
    private Logger logger = LoggerFactory.getLogger(ServiceParamBuildService.class);
    private Map<Long, Pair<Long, String[]>> paramTypeMap = Maps.newHashMap();

    /**
     * build param vlaue
     *
     * @param fieldDefinitionList
     * @param messageBody
     * @return
     * @throws Exception
     */
    public Object[] buildParamValue(List<FieldDefinition> fieldDefinitionList, JSONObject messageBody) throws Exception {
        List<Object> result = Lists.newArrayList();
        for (FieldDefinition fieldDefinition : fieldDefinitionList) {
            Object v = getValue(fieldDefinition, messageBody);
            result.add(v);
        }

        return result.toArray();
    }


    private Object getValue(FieldDefinition fieldDefinition, JSONObject messageBody) throws Exception {
        String fieldType = fieldDefinition.getFieldType();
        if (DataTypeTranslator.isDate(fieldType)) {
            String valueRefExpress = fieldDefinition.getRef();
            String[] split = StringUtils.split(valueRefExpress, ".");
            Date dateValue = getDateValue(fieldDefinition.getFieldFormat(), split, messageBody);
            return dateValue;
        } else if (DataTypeTranslator.isPrimitive(fieldType)) {
            String valueRefExpress = fieldDefinition.getRef();
            String[] split = StringUtils.split(valueRefExpress, ".");
            Object value = getFieldValue(fieldDefinition.getFieldType(), split, messageBody);
            return value;
        } else if (DataTypeTranslator.isArray(fieldType)) {
            String valueRefExpress = fieldDefinition.getRef();
            String[] split = StringUtils.split(valueRefExpress, ".");
            Object value = getTargetValue(split, messageBody);
            if (value == null) {
                return null;
            }
            if (value instanceof List) {
                List value1 = (List) value;
                return value1.toArray();
            } else if (DataTypeTranslator.isArray(value.getClass().getTypeName())) {
                return value;
            } else {
                throw new RuntimeException("param " + fieldDefinition.getFieldName() + " type is  " + fieldDefinition.getFieldType() + " but value type is " + value.getClass());
            }
        } else if (CollectionUtils.isNotEmpty(fieldDefinition.getSubFields())) {
            Map<String, Object> valueMap = Maps.newHashMap();
            for (FieldDefinition subField : fieldDefinition.getSubFields()) {
                Object value = getValue(subField, messageBody);
                valueMap.put(subField.getFieldName(), value);
            }
            return valueMap;
        } else {
            String valueRefExpress = fieldDefinition.getRef();
            String[] split = StringUtils.split(valueRefExpress, ".");
            return getTargetValue(split, messageBody);
        }
    }

    private <T> T getTargetValue(String[] split, JSONObject messageBody) {
        return getValue(split, 0, messageBody);
    }

    /**
     * this method is compatibility when the data type is not validate
     *
     * @param fieldType
     * @param split
     * @param messageBody
     * @return
     */
    private Object getFieldValue(String fieldType, String[] split, JSONObject messageBody) {
        Object valueStr = getValue(split, 0, messageBody);
        if (valueStr == null) {
            return null;
        }
        return DataTypeTranslator.getStringCastToPrimitiveFunctions().get(fieldType).apply(valueStr.toString());
    }

    private Date getDateValue(String format, String[] split, JSONObject messageBody) throws Exception {
        String valueStr = getValue(split, 0, messageBody);
        if (StringUtils.isBlank(format)) {
            throw new RuntimeException("data time must have expression");
        }
        DateFormat dateFormat = new SimpleDateFormat(format);
        Date parse = dateFormat.parse(valueStr);
        return parse;
    }

    private <T> T getValue(String[] split, int i, JSONObject messageBody) {
        if (split == null || split.length == 0) {
            return null;
        }
        if (split.length == i + 1) {
            return (T) messageBody.get(split[i]);
        }
        Object o = messageBody.get(split[i]);
        if (o instanceof Map) {
            Object value = getValue(split, i + 1, (JSONObject) o);
            return (T) value;
        }
        throw new RuntimeException("data type not right ");
    }

    public String[] buildParamType(RpcServiceMethodInfo rpcServiceMethodInfo) {
        Long timestamp = rpcServiceMethodInfo.getTimestamp();
        if (paramTypeMap.containsKey(rpcServiceMethodInfo.getId()) && timestamp.equals(paramTypeMap.get(rpcServiceMethodInfo.getId()).getLeft())) {
            return paramTypeMap.get(rpcServiceMethodInfo.getId()).getRight();
        }
        List<String> paramType = rpcServiceMethodInfo.getParamFieldList().stream().map(fieldDefinition -> fieldDefinition.getFieldType()).collect(Collectors.toList());
        String[] strings = paramType.toArray(new String[paramType.size()]);
        paramTypeMap.put(rpcServiceMethodInfo.getId(), Pair.of(timestamp, strings));
        return strings;
    }


}
