package com.scheduler_service.configuration;

import com.scheduler_service.models.JobEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class KafkaProducerConfig {

    private final ServiceConfiguration serviceConfiguration;

    @Bean
    public ProducerFactory<String, JobEvent> producerFactory() {

        Map<String, Object> props = new HashMap<>();
        props.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                serviceConfiguration.getKafka().getBootstrapServers()
        );

        props.put(ProducerConfig.ACKS_CONFIG,
                serviceConfiguration.getKafka().getProducer().getAcks());

        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG,
                serviceConfiguration.getKafka().getProducer().isEnableIdempotence());

        props.put(ProducerConfig.RETRIES_CONFIG,
                serviceConfiguration.getKafka().getProducer().getRetries());

        props.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG,
                serviceConfiguration.getKafka().getProducer().getRetryBackoffMs());

        return new DefaultKafkaProducerFactory<>(
                props,
                new StringSerializer(),
                new JsonSerializer<>()
        );
    }


    @Bean
    public KafkaTemplate<String, JobEvent> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
