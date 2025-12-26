package com.scheduler_service.repository;


import com.scheduler_service.models.dao.JobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;


@Repository
public interface JobRepository extends JpaRepository<JobEntity, String> {


    @Query("""
    SELECT MIN(j.nextRunAt)
    FROM JobEntity j
    WHERE j.status = 'SCHEDULED'
""")
    Instant findNextRunTime();

    @Query(
            value = """
            SELECT *
            FROM jobs
            WHERE status = 'SCHEDULED'
              AND next_run_at <= now()
            ORDER BY next_run_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true
    )
    List<JobEntity> lockReadyJobs(@Param("limit") int limit);
}
