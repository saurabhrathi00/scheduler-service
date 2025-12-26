package com.scheduler_service.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "configs")
@Data
public class ServiceConfiguration {

    private JobDb jobDb;
    private Kafka kafka;

    @Data
    public static class JobDb {
        private String name;
        private String url;
    }

    @Data
    public static class Kafka {
        private String bootstrapServers;
        private Topic topic;
        private Producer producer;

        @Data
        public static class Topic {
            private String jobEvents;
        }

        @Data
        public static class Producer {
            private String acks;
            private boolean enableIdempotence;
            private int retries;
            private int retryBackoffMs;
        }
    }
}

