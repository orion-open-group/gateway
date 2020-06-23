package com.orion.mobile.gateway.domain;

import lombok.Data;

import java.io.Serializable;
import java.util.List;


@Data
public class RpcServiceMethodInfo implements Serializable {
    private Long id;
    private Long serviceId;
    private String methodAlias;
    private String methodName;
    private String description;
    private Boolean isValid;
    private Boolean isDeleted;
    private ReturnConfig returnConfig;
    private Long  timestamp;
    private List<FieldDefinition> paramFieldList;
}
