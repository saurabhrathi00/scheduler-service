package com.scheduler_service.models.dao;


import com.scheduler_service.models.enums.ExecutionMode;
import com.scheduler_service.models.enums.JobStatus;
import com.scheduler_service.models.enums.JobType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "jobs",
        indexes = {
                @Index(name = "idx_jobs_next_run", columnList = "next_run_at"),
                @Index(name = "idx_jobs_status", columnList = "status"),
                @Index(name = "idx_jobs_created_by", columnList = "created_by")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobEntity {
        @Id
        @Column(name = "job_id", nullable = false, updatable = false)
        private String jobId;

        @Enumerated(EnumType.STRING)
        @Column(name = "job_type", nullable = false)
        private JobType jobType;

        @Enumerated(EnumType.STRING)
        @Column(name = "execution_mode")
        private ExecutionMode executionMode;

        @Column(name = "cron_expression")
        private String cronExpression;

        /**
         * Stored as JSON (scheduler treats this as opaque)
         */
        @JdbcTypeCode(SqlTypes.JSON)
        @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
        private Map<String, Object> payload;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false)
        private JobStatus status;

        @Column(name = "attempts", nullable = false)
        private int attempts;

        @Column(name = "next_run_at", nullable = false)
        private Instant nextRunAt;

        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt;

        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;

        @Column(name = "created_by", nullable = false)
        private String createdBy;


        @PrePersist
        public void onCreate() {
                Instant now = Instant.now();
                this.createdAt = now;
                this.updatedAt = now;
        }

        @PreUpdate
        public void onUpdate() {
                this.updatedAt = Instant.now();
        }
}
