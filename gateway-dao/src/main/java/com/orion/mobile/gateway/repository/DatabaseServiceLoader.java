package com.orion.mobile.gateway.repository;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.pool.DruidPooledConnection;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.LoggerFactory;
import com.orion.mobile.gateway.domain.FieldDefinition;
import com.orion.mobile.gateway.domain.ReturnConfig;
import com.orion.mobile.gateway.domain.RpcServiceInfo;
import com.orion.mobile.gateway.domain.RpcServiceMethodInfo;
import com.orion.mobile.gateway.enums.ServiceOperType;
import com.orion.mobile.gateway.listener.ServiceListener;
import com.orion.mobile.gateway.service.ScheduleService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/31 9:14
 * @Version 1.0.0
 */
@Singleton
public class DatabaseServiceLoader implements ServiceLoader {

    private Logger logger = LoggerFactory.getLogger(DatabaseServiceLoader.class);

    @Inject
    DruidDataSource druidDataSource;

    @Inject
    ScheduleService scheduleService;


    @Override
    public List<RpcServiceMethodInfo> listAllServiceMethodByApp(String appId) throws Exception {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = druidDataSource.getConnection();
            if (StringUtils.isNotEmpty(appId)) {
                preparedStatement = connection.prepareStatement("select * from service_method_info where service_id in ( select id from service_info where app_id = ?) ");
                preparedStatement.setString(1, appId);
            } else {
                preparedStatement = connection.prepareStatement("select * from service_method_info ");
            }
            resultSet = preparedStatement.executeQuery();
            return buildServiceMethod(resultSet);
        } catch (Throwable e) {
            logger.error("execute query error", e);
        } finally {
            close(resultSet, preparedStatement, connection);
        }
        return Lists.newArrayList();
    }

    @Override
    public List<RpcServiceMethodInfo> listRecentUpdateServiceMethod(String appId, int second) throws Exception {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = druidDataSource.getConnection();
            if (StringUtils.isNotEmpty(appId)) {
                preparedStatement = connection.prepareStatement("select * from service_method_info where service_id in ( select id from service_info where app_id = ?) and timestampdiff(SECOND,modify_time,now()) < ?");
                preparedStatement.setString(1, appId);
                preparedStatement.setInt(2, second);
            } else {
                preparedStatement = connection.prepareStatement("select * from service_method_info where timestampdiff(SECOND,modify_time,now()) < ?");
                preparedStatement.setInt(1, second);
            }
            resultSet = preparedStatement.executeQuery();
            return buildServiceMethod(resultSet);
        } catch (Throwable e) {
            logger.error("execute query error", e);
        } finally {
            close(resultSet, preparedStatement, connection);
        }
        return Lists.newArrayList();
    }

    /**
     * how many changes
     *
     * @param appId
     * @param second
     * @return
     * @throws Exception
     */
    public int listRecentUpdateServiceMethodCnt(String appId, int second) throws Exception {
        DruidPooledConnection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            connection = druidDataSource.getConnection();
            if (StringUtils.isNotEmpty(appId)) {
                preparedStatement = connection.prepareStatement("select count(1) as Cnt from service_method_info where service_id in ( select id from service_info where app_id = ?) and timestampdiff(SECOND,modify_time,now()) < ?");
                preparedStatement.setString(1, appId);
                preparedStatement.setInt(2, second);
            } else {
                preparedStatement = connection.prepareStatement("select count(1) as Cnt from service_method_info where timestampdiff(SECOND,modify_time,now()) < ?");
                preparedStatement.setInt(1, second);
            }
            resultSet = preparedStatement.executeQuery();
            resultSet.next();
            int cnt = resultSet.getInt("Cnt");
            return cnt;
        } catch (Throwable e) {
            logger.error("execute query error", e);
        } finally {
            close(resultSet, preparedStatement, connection);
        }
        return 0;
    }

    /**
     * release resource
     *
     * @param closeable
     */
    private void close(AutoCloseable... closeable) {
        for (AutoCloseable closeable1 : closeable) {
            try {
                if (closeable1 != null) {
                    closeable1.close();
                }
            } catch (Throwable e) {
                logger.error("close fail", e);
            }
        }
    }


    /**
     * build service method
     *
     * @param rs
     * @return
     * @throws Exception
     */
    private List<RpcServiceMethodInfo> buildServiceMethod(ResultSet rs) throws Exception {
        List<RpcServiceMethodInfo> rpcServiceMethodInfoList = Lists.newArrayList();
        while (rs.next()) {
            try {
                RpcServiceMethodInfo rpcServiceMethodInfo = new RpcServiceMethodInfo();
                rpcServiceMethodInfo.setId(rs.getLong("id"));
                rpcServiceMethodInfo.setServiceId(rs.getLong("service_id"));
                rpcServiceMethodInfo.setIsValid(rs.getBoolean("is_valid"));
                rpcServiceMethodInfo.setIsDeleted(rs.getBoolean("is_deleted"));
                rpcServiceMethodInfo.setMethodAlias(rs.getString("method_alias"));
                rpcServiceMethodInfo.setMethodName(rs.getString("method_name"));
                rpcServiceMethodInfo.setTimestamp(rs.getTimestamp("modify_time").getTime());
                if (StringUtils.isNotEmpty(rs.getString("return_config"))) {
                    ReturnConfig returnConfig = JSON.parseObject(rs.getString("return_config"), ReturnConfig.class);
                    rpcServiceMethodInfo.setReturnConfig(returnConfig);
                }
                List<FieldDefinition> fieldDefinitions = JSON.parseArray(rs.getString("param_fields"), FieldDefinition.class);
                rpcServiceMethodInfo.setParamFieldList(fieldDefinitions);
                rpcServiceMethodInfoList.add(rpcServiceMethodInfo);
            } catch (Throwable e) {
                logger.error("service method config not validate ", e);
            }
        }
        return rpcServiceMethodInfoList;
    }


    @Override
    public List<RpcServiceInfo> listRecentUpdateService(String appId, int second) throws Exception {
        PreparedStatement preparedStatement = null;
        DruidPooledConnection connection = null;
        ResultSet rs = null;
        try {
            connection = druidDataSource.getConnection();
            if (StringUtils.isNotEmpty(appId)) {
                preparedStatement = connection.prepareStatement("select * from service_info where app_id =? and timestampdiff(SECOND,modify_time,now()) < ?");
                preparedStatement.setString(1, appId);
                preparedStatement.setInt(2, second);
            } else {
                preparedStatement = connection.prepareStatement("select * from service_info where timestampdiff(SECOND,modify_time,now()) < ?");
                preparedStatement.setInt(1, second);
            }
            rs = preparedStatement.executeQuery();
            return buildServiceInfo(rs);
        } catch (Throwable e) {
            logger.error("query exception ", e);
        } finally {
            close(rs, preparedStatement, connection);
        }
        return Lists.newArrayList();
    }

    /**
     * query how many changes in the last second times
     *
     * @param appId
     * @param second
     * @return
     * @throws Exception
     */
    public int listRecentUpdateServiceCnt(String appId, int second) throws Exception {
        PreparedStatement preparedStatement = null;
        DruidPooledConnection connection = null;
        ResultSet rs = null;
        try {
            connection = druidDataSource.getConnection();
            if (StringUtils.isNotEmpty(appId)) {
                preparedStatement = connection.prepareStatement("select count(1) as Cnt from service_info where app_id =? and timestampdiff(SECOND,modify_time,now()) < ?");
                preparedStatement.setString(1, appId);
                preparedStatement.setInt(2, second);
            } else {
                preparedStatement = connection.prepareStatement("select count(1) as Cnt from service_info where timestampdiff(SECOND,modify_time,now()) < ?");
                preparedStatement.setInt(1, second);
            }
            rs = preparedStatement.executeQuery();
            rs.next();
            int cnt = rs.getInt("Cnt");
            return cnt;
        } catch (Throwable e) {
            logger.error("query exception ", e);
        } finally {
            close(rs, preparedStatement, connection);
        }
        return 0;
    }

    @Override
    public List<RpcServiceInfo> listAllServiceInfo(String appId) throws Exception {
        PreparedStatement preparedStatement = null;
        DruidPooledConnection connection = null;
        ResultSet rs = null;
        try {
            connection = druidDataSource.getConnection();
            if (StringUtils.isNotEmpty(appId)) {
                preparedStatement = connection.prepareStatement("select * from service_info where app_id =?");
                preparedStatement.setString(1, appId);
            } else {
                preparedStatement = connection.prepareStatement("select * from service_info ");
            }
            rs = preparedStatement.executeQuery();
            return buildServiceInfo(rs);
        } catch (Throwable e) {
            logger.error("query exception ", e);
        } finally {
            close(rs, preparedStatement, connection);
        }
        return Lists.newArrayList();
    }

    private List<RpcServiceInfo> buildServiceInfo(ResultSet rs) throws SQLException {
        List<RpcServiceInfo> rpcServicInfoList = Lists.newArrayList();
        while (rs.next()) {
            RpcServiceInfo rpcServicInfo = new RpcServiceInfo();
            rpcServicInfo.setId(rs.getLong("id"));
            rpcServicInfo.setIsValid(rs.getBoolean("is_valid"));
            rpcServicInfo.setIsDeleted(rs.getBoolean("is_deleted"));
            rpcServicInfo.setServiceAlias(rs.getString("service_alias"));
            rpcServicInfo.setAppId(rs.getString("app_id"));
            rpcServicInfo.setAppAlias(rs.getString("app_alias"));
            rpcServicInfo.setServiceType(rs.getString("service_type"));
            rpcServicInfo.setServiceVersion(rs.getString("service_version"));
            rpcServicInfo.setServiceConfig(rs.getString("service_config"));
            rpcServicInfo.setTimestamp(rs.getTimestamp("modify_time").getTime());
            JSONObject jsonObject = JSON.parseObject(rpcServicInfo.getServiceConfig());
            rpcServicInfo.setServiceConfigMap(jsonObject);
            rpcServicInfoList.add(rpcServicInfo);
        }
        return rpcServicInfoList;
    }

    @Override
    public void registerListener(ServiceListener serviceListener) {
        scheduleService.scheduleAtFixedRate(() -> {
            try {
                int i = listRecentUpdateServiceMethodCnt(null, 5);
                if (i == 0) {
                    return;
                }
                logger.info("there is {} service method changes in the last {} second", i, 5);
                List<RpcServiceMethodInfo> rpcServiceMethodInfoList = listRecentUpdateServiceMethod(null, 5);
                if (CollectionUtils.isNotEmpty(rpcServiceMethodInfoList)) {
                    rpcServiceMethodInfoList.stream().forEach(rpcServiceMethodInfo -> {
                        if (!rpcServiceMethodInfo.getIsDeleted() && rpcServiceMethodInfo.getIsValid()) {
                            //add service
                            serviceListener.notifyServiceMethodChange(ServiceOperType.ADD, rpcServiceMethodInfo);
                        } else {
                            if (rpcServiceMethodInfo.getIsDeleted()) {
                                serviceListener.notifyServiceMethodChange(ServiceOperType.DELETE, rpcServiceMethodInfo);
                            } else {
                                if (!rpcServiceMethodInfo.getIsValid()) {
                                    serviceListener.notifyServiceMethodChange(ServiceOperType.DISABLE, rpcServiceMethodInfo);
                                } else {
                                    serviceListener.notifyServiceMethodChange(ServiceOperType.ENABLE, rpcServiceMethodInfo);
                                }
                            }
                        }
                    });
                }
            } catch (Throwable e) {
                logger.error("load recent change service method exception ", e);
            }
        }, 1, 2, TimeUnit.SECONDS);
        scheduleService.scheduleAtFixedRate(() -> {
            try {
                int i = listRecentUpdateServiceCnt(null, 5);
                if (i == 0) {
                    return;
                }
                logger.info("there is {} service changes in the last {} second", i, 5);
                List<RpcServiceInfo> rpcServiceInfoList = listRecentUpdateService(null, 5);
                if (CollectionUtils.isNotEmpty(rpcServiceInfoList)) {
                    rpcServiceInfoList.stream().forEach(rpcServiceInfo -> {
                        if (!rpcServiceInfo.getIsDeleted() && rpcServiceInfo.getIsValid()) {
                            //add service
                            serviceListener.notifyServiceChange(ServiceOperType.ADD, rpcServiceInfo);
                        } else {
                            if (rpcServiceInfo.getIsDeleted()) {
                                serviceListener.notifyServiceChange(ServiceOperType.DELETE, rpcServiceInfo);
                            } else {
                                if (!rpcServiceInfo.getIsValid()) {
                                    serviceListener.notifyServiceChange(ServiceOperType.DISABLE, rpcServiceInfo);
                                } else {
                                    serviceListener.notifyServiceChange(ServiceOperType.ENABLE, rpcServiceInfo);
                                }
                            }
                        }
                    });
                }
            } catch (Throwable e) {
                logger.error("load recent change service method exception ", e);
            }
        }, 1, 2, TimeUnit.SECONDS);
    }
}
