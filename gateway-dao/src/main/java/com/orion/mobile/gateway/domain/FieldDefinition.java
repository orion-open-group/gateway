package com.orion.mobile.gateway.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FieldDefinition {
    private String fieldType;
    private String fieldName;
    private String fieldFormat;
    private String ref;
    private List<FieldDefinition> subFields= new ArrayList<>();
}
