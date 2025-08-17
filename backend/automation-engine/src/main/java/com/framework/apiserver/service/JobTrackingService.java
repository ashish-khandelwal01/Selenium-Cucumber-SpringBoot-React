package com.framework.apiserver.service;

import com.framework.apiserver.config.JobStatus;
import com.framework.apiserver.dto.JobStatusSummary;
import com.framework.apiserver.entity.JobTracking;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing job tracking operations.
 * Provides methods for creating, updating, and monitoring jobs.
 */
public interface JobTrackingService {

    /**
     * Starts an asynchronous job with the given parameters.
     *
     * @param runId the run identifier for grouping related jobs
     * @param tag the job tag for identification
     * @param createdBy the user who created the job
     * @return the unique job ID
     */
    String startAsyncJob(String runId, String tag, String createdBy);

    /**
     * Starts a synchronous job with the given parameters.
     *
     * @param tag the job tag for identification (also used as runId for sync jobs)
     * @param createdBy the user who created the job
     * @return the unique job ID
     */
    String startSyncJob(String tag, String createdBy);

    /**
     * Updates the status of a job.
     *
     * @param jobId the job ID to update
     * @param status the new status
     */
    void updateJobStatus(String jobId, JobStatus status);

    /**
     * Updates the status of a job with an error message.
     *
     * @param jobId the job ID to update
     * @param status the new status
     * @param errorMessage the error message (can be null)
     */
    void updateJobStatus(String jobId, JobStatus status, String errorMessage);

    /**
     * Completes a job with the given status.
     *
     * @param jobId the job ID to complete
     * @param status the completion status
     */
    void completeJob(String jobId, JobStatus status);

    /**
     * Attempts to cancel an active job.
     *
     * @param jobId the job ID to cancel
     * @return true if the job was successfully cancelled, false otherwise
     */
    boolean cancelJob(String jobId);

    /**
     * Gets a summary of job statuses including active job counts by type.
     *
     * @return the job status summary
     */
    JobStatusSummary getJobStatusSummary();

    /**
     * Retrieves all currently active jobs.
     *
     * @return list of active jobs ordered by start time
     */
    List<JobTracking> getActiveJobs();

    /**
     * Retrieves all active jobs with the specified tag.
     *
     * @param tag the job tag to filter by
     * @return list of active jobs with the given tag
     */
    List<JobTracking> getJobsByTag(String tag);

    /**
     * Retrieves a job by its ID.
     *
     * @param jobId the job ID to search for
     * @return Optional containing the job if found, empty otherwise
     */
    Optional<JobTracking> getJobById(String jobId);

    /**
     * Retrieves all active jobs with the specified run ID.
     *
     * @param runId the run ID to filter by
     * @return list of active jobs with the given run ID
     */
    List<JobTracking> getJobsByRunId(String runId);

    /**
     * Cleans up old completed jobs.
     * A scheduled task typically calls this method.
     */
    void cleanupOldJobs();
}