package com.scheduler_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobEvent {
    private String jobId;
    private int attempt;
    private Instant scheduledAt;
}

