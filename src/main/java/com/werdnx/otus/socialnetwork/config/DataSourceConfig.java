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

    @Bean
    public DataSource masterDataSource(
            DataSourceProperties propsMaster) {
        // По-умолчанию initializeDataSourceBuilder() вернёт HikariDataSource
        return propsMaster.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    public DataSource slaveDataSource(
            @Qualifier("slave") DataSourceProperties propsSlave) {
        return propsSlave.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.master")
    public DataSourceProperties propsMaster() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.slave")
    public DataSourceProperties propsSlave() {
        return new DataSourceProperties();
    }
}
