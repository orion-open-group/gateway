package com.orion.mobile.gateway.listener;

import com.orion.mobile.gateway.domain.RpcServiceInfo;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;
import com.orion.mobile.gateway.enums.ServiceOperType;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/31 17:12
 * @Version 1.0.0
 */
public interface ServiceListener {

    public void notifyServiceChange(ServiceOperType serviceOperType, RpcServiceInfo rpcServiceInfo);

    public void notifyServiceMethodChange(ServiceOperType serviceOperType, RpcServiceMethodInfo rpcServiceMethodInfo);
}
