package com.orion.mobile.gateway.invoke;

import com.alibaba.fastjson.JSONObject;
import com.orion.mobile.gateway.domain.RpcServiceInfo;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;

public interface ServiceInvoker {
    Object invoke(RpcServiceInfo rpcServiceInfo,String deviceToken,  String userToken, RpcServiceMethodInfo rpcServiceMethodInfo, JSONObject messageBody) throws Exception;

    String code();
}
