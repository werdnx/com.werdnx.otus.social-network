package com.werdnx.otus.socialnetwork.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource routingDataSource(
            @Qualifier("masterDataSource") DataSource master,
            @Qualifier("slaveDataSource") DataSource slave) {

        ReplicationRoutingDataSource routing = new ReplicationRoutingDataSource();
        Map<Object, Object> dsMap = new HashMap<>();
        dsMap.put("master", master);
        dsMap.put("slave", slave);
        routing.setTargetDataSources(dsMap);
        routing.setDefaultTargetDataSource(master);
        return routing;
    }

    @Bean("masterDataSource")
    public DataSource masterDataSource(
            @Qualifier("masterProps") DataSourceProperties props) {
        return props
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("slaveDataSource")
    public DataSource slaveDataSource(
            @Qualifier("slaveProps") DataSourceProperties props) {
        return props
                .initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean("masterProps")
    @ConfigurationProperties("spring.datasource.master")
    public DataSourceProperties masterProperties() {
        return new DataSourceProperties();
    }

    @Bean("slaveProps")
    @ConfigurationProperties("spring.datasource.slave")
    public DataSourceProperties slaveProperties() {
        return new DataSourceProperties();
    }
}
