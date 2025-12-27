package com.scheduler_service.producer.kafka;

import com.scheduler_service.models.JobEvent;

public interface JobDispatcher {

    void dispatch(JobEvent event);
}
