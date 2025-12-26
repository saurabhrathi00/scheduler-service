package com.scheduler_service.services;

import com.scheduler_service.models.dao.JobEntity;
import com.scheduler_service.models.enums.JobStatus;
import com.scheduler_service.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {

    private final JobRepository jobRepository;


    @Transactional
    public List<JobEntity> claimReadyJobs(int batchSize) {

        List<JobEntity> jobs = jobRepository.lockReadyJobs(batchSize);

        if (jobs.isEmpty()) {
            return jobs;
        }

        Instant now = Instant.now();

        for (JobEntity job : jobs) {
            job.setStatus(JobStatus.RUNNING);
            job.setAttempts(job.getAttempts() + 1);
            job.setUpdatedAt(now);
        }

        log.debug("Claimed {} job(s) for scheduling", jobs.size());

        return jobs;
    }


    @Transactional(readOnly = true)
    public Instant findNextRunTime() {
        return jobRepository.findNextRunTime();
    }
}
