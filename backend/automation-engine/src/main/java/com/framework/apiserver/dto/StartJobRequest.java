package com.framework.apiserver.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) representing a request to start a job.
 * Contains information required to initiate a job execution.
 */
@Data
public class StartJobRequest {

    /**
     * The tag associated with the job, used for categorization or filtering.
     */
    private String tag;

    /**
     * The user or system that created the job.
     */
    private String createdBy;

    /**
     * The run ID associated with the job, used for reruns.
     * This field is optional.
     */
    private String runId;
}