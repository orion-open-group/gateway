package com.orion.mobile.gateway.module;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.netflix.config.ConfigurationManager;
import com.orion.mobile.gateway.repository.DatabaseServiceLoader;
import com.orion.mobile.gateway.repository.ServiceLoader;
import com.orion.mobile.gateway.util.GatewayEncrypt;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/10/30 20:07
 * @Version 1.0.0
 */
@Singleton
public class ServiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ServiceLoader.class).to(DatabaseServiceLoader.class);
    }

    @Provides
    public DruidDataSource druidDataSource() throws Exception {
        DruidDataSource druidDataSource = new DruidDataSource();
        final AbstractConfiguration configInstance = ConfigurationManager.getConfigInstance();
        String token = configInstance.getString("datasource.token");
        if (StringUtils.isNotEmpty(token)) {
            String decrypt = GatewayEncrypt.decrypt(token);
            JSONObject jsonObject = JSON.parseObject(decrypt);
            druidDataSource.setUrl(jsonObject.getString("url"));
            druidDataSource.setUsername(jsonObject.getString("user"));
            druidDataSource.setPassword(jsonObject.getString("pwd"));
        } else {
            druidDataSource.setUsername(GatewayEncrypt.decrypt(configInstance.getString("datasource.username")));
            druidDataSource.setPassword(GatewayEncrypt.decrypt(configInstance.getString("datasource.password")));
            druidDataSource.setUrl(GatewayEncrypt.decrypt(configInstance.getString("datasource.url")));
        }
        druidDataSource.setMaxActive(configInstance.getInteger("datasource.active", 2));
        druidDataSource.setDefaultAutoCommit(false);
        druidDataSource.init();
        return druidDataSource;
    }

}
