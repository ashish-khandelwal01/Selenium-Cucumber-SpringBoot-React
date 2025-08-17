package com.framework.apiserver.entity;

import com.framework.apiserver.config.JobStatus;
import com.framework.apiserver.config.JobType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a job tracking record.
 * Stores information about jobs, including their status, type, and execution details.
 */
@Entity
@Table(name = "job_tracking")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobTracking {

    /**
     * The unique identifier for the job.
     */
    @Id
    private String jobId;

    /**
     * The run ID associated with the job.
     */
    @Column(name = "run_id")
    private String runId;

    /**
     * The type of the job (e.g., specific job category or classification).
     */
    @Enumerated(EnumType.STRING)
    private JobType type;

    /**
     * The tag associated with the job, used for categorization or filtering.
     */
    private String tag;

    /**
     * The current status of the job (e.g., CREATED, RUNNING, COMPLETED).
     */
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    /**
     * The start time of the job execution.
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;

    /**
     * The end time of the job execution.
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * The user or system that created the job.
     */
    @Column(name = "created_by")
    private String createdBy;

    /**
     * The error message, if any, associated with the job execution.
     */
    @Column(name = "error_message")
    private String errorMessage;

    /**
     * The name of the thread in which the job was executed.
     */
    @Column(name = "thread_name")
    private String threadName;
}