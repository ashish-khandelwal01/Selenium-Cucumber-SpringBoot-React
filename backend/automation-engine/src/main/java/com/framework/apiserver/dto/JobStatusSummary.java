package com.framework.apiserver.dto;

import com.framework.apiserver.entity.JobTracking;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object (DTO) representing a summary of job statuses.
 * Provides an overview of active jobs, including their counts and details.
 */
@Data
@Builder
public class JobStatusSummary {

    /**
     * The total number of active jobs.
     */
    private int totalActiveJobs;

    /**
     * The number of asynchronous jobs currently active.
     */
    private int asyncJobs;

    /**
     * The number of synchronous jobs currently active.
     */
    private int syncJobs;

    /**
     * A list of active jobs with their details.
     */
    private List<JobTracking> activeJobs;
}