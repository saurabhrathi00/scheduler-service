package com.scheduler_service.producer.kafka;

import com.scheduler_service.models.dao.JobEvent;

public interface JobDispatcher {

    void dispatch(JobEvent event);
}
