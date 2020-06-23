package com.orion.mobile.gateway.repository;

import com.orion.mobile.gateway.domain.RpcServiceInfo;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;
import com.orion.mobile.gateway.listener.ServiceListener;

import java.util.List;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/31 9:14
 * @Version 1.0.0
 */
public interface ServiceLoader {

    /**
     * list all service method
     *
     * @param appId
     * @return
     * @throws Exception
     */
    public List<RpcServiceMethodInfo> listAllServiceMethodByApp(String appId) throws Exception;

    /**
     * list new update service method
     *
     * @param appId
     * @param second
     * @return
     * @throws Exception
     */
    public List<RpcServiceMethodInfo> listRecentUpdateServiceMethod(String appId, int second) throws Exception;

    /**
     * @param appId
     * @return
     * @throws Exception
     */
    public List<RpcServiceInfo> listRecentUpdateService(String appId, int second) throws Exception;

    /**
     * @param appId
     * @return
     * @throws Exception
     */
    public List<RpcServiceInfo> listAllServiceInfo(String appId) throws Exception;


    /**
     * register listener
     *
     * @param serviceListener
     */
    public void registerListener(ServiceListener serviceListener);

}

