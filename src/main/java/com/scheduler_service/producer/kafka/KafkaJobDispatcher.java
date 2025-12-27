package com.scheduler_service.producer.kafka;

import com.scheduler_service.configuration.ServiceConfiguration;
import com.scheduler_service.models.JobEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaJobDispatcher implements JobDispatcher {

    private final KafkaTemplate<String, JobEvent> kafkaTemplate;
    private final ServiceConfiguration serviceConfiguration;

    @Override
    public void dispatch(JobEvent event) {
        kafkaTemplate.send(
                serviceConfiguration.getKafka().getTopic().getJobEvents(),
                event.getJobId(),
                event
        );
    }
}
