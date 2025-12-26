package com.scheduler_service.configuration;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    private final SecretsConfiguration secretsProperties;
    private final ServiceConfiguration serviceConfiguration;

    @Bean
    @Primary
    public DataSource dataSource() {
        SecretsConfiguration.Datasource db = secretsProperties.getDatasource();

        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(serviceConfiguration.getJobDb().getUrl() + serviceConfiguration.getJobDb().getName());
        ds.setUsername(db.getUsername());
        ds.setPassword(db.getPassword());
        ds.setDriverClassName(db.getDriverClassName());
        return ds;
    }
}
