package com.scheduler_service.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "secrets")
public class SecretsConfiguration {

    private Jwt jwt;
    private Datasource datasource;

    @Data
    public static class Jwt {
        private String secret;
    }

    @Data
    public static class Datasource {
        private String username;
        private String password;
        private String driverClassName;
    }
}

