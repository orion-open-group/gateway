package com.orion.mobile.gateway.domain;

import lombok.Data;

import java.util.List;


/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/11/2 17:29
 * @Version 1.0.0
 */
@Data
public class ReturnConfig {
    private List<String> dataRef;
    private String codeRef;
    private String msgRef;
    private String sucRef;
}
