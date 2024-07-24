package com.app.server.config;

import com.ygame.framework.common.Config;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@EntityScan(basePackages = {"com.app.server.data.entity"})
@EnableAutoConfiguration
public class DatasourceConfig {
    @Bean
    public DataSource getDataSource() {
        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.driverClassName(Config.getParam("database", "driver"));
        dataSourceBuilder.url(Config.getParam("database", "url"));
        dataSourceBuilder.username(Config.getParam("database", "user"));
        dataSourceBuilder.password(Config.getParam("database", "password"));
        return dataSourceBuilder.build();
    }
}