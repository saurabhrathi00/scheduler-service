package com.scheduler_service.scheduler;

import com.scheduler_service.configuration.ServiceConfiguration;
import com.scheduler_service.models.dao.JobEntity;
import com.scheduler_service.models.JobEvent;
import com.scheduler_service.producer.kafka.JobDispatcher;
import com.scheduler_service.services.SchedulerService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SchedulerRunner {

    private final SchedulerService schedulerService;
    private final JobDispatcher jobEventProducer;
    private final ServiceConfiguration serviceConfiguration;

    private static final int MAX_INLINE_RETRIES = 3;

    @PostConstruct
    public void start() {
        Thread schedulerThread = new Thread(this::runLoop, "job-scheduler");
        schedulerThread.setDaemon(true);
        schedulerThread.start();
        log.info("Job Scheduler started");
    }

    private void runLoop() {
        while (true) {
            try {
                runOnce();
                sleep(computeSleepMillis());
            } catch (Throwable t) {
                t.printStackTrace();
                sleep(serviceConfiguration.getScheduler().getMaxSleepMs());
            }
        }
    }

    private void runOnce() {
        List<JobEntity> jobs = schedulerService.claimReadyJobs(serviceConfiguration.getScheduler().getBatchSize());
        if (jobs.isEmpty()) {
            return;
        }
        log.info("Claimed {} job(s) for execution", jobs.size());
        for (JobEntity job : jobs) {
            dispatch(job);
        }
    }

    private void dispatch(JobEntity job) {

        JobEvent event = JobEvent.builder()
                .jobId(job.getJobId())
                .attempt(job.getAttempts())
                .scheduledAt(Instant.now())
                .build();

        int retry = 0;

        while (true) {
            try {
                jobEventProducer.dispatch(event);

                log.debug(
                        "Dispatched jobId={} attempt={}",
                        job.getJobId(),
                        job.getAttempts()
                );
                return;
            } catch (Exception ex) {
                retry++;
                if (!isRetriableKafkaException(ex) || retry > MAX_INLINE_RETRIES) {
                    log.error(
                            "Failed to dispatch jobId={} after {} attempt(s)",
                            job.getJobId(),
                            retry,
                            ex
                    );
                    return;
                }
                long backoff = serviceConfiguration.getScheduler().getBaseBackoffMs() * retry;
                log.warn(
                        "Retrying dispatch for jobId={} (retry={}, backoff={}ms)",
                        job.getJobId(),
                        retry,
                        backoff
                );
                sleep(backoff);
            }
        }
    }


    private long computeSleepMillis() {

        Instant nextRunAt = schedulerService.findNextRunTime();

        if (nextRunAt == null) {
            return serviceConfiguration.getScheduler().getMaxSleepMs();
        }

        long delayMs = Duration.between(
                Instant.now(),
                nextRunAt
        ).toMillis();

        if (delayMs <= 0) {
            return 0;
        }

        return Math.min(delayMs, serviceConfiguration.getScheduler().getMaxSleepMs());
    }

    private void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {
        }
    }

    private boolean isRetriableKafkaException(Throwable ex) {

        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof org.apache.kafka.common.errors.RetriableException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

}

