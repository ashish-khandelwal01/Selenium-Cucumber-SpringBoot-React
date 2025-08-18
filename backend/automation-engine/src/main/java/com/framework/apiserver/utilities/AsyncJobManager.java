package com.framework.apiserver.utilities;

import com.framework.apiserver.config.JobStatus;
import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.service.JobTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for managing asynchronous jobs.
 * Provides methods to create, track, update, and manage the lifecycle of asynchronous jobs.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncJobManager {

    // Map to store the status of jobs by their IDs
    private final Map<String, JobStatus> jobStatusMap = new ConcurrentHashMap<>();
    // Map to store the threads associated with jobs by their IDs
    private final Map<String, Thread> jobThreadMap = new ConcurrentHashMap<>();
    // Map to store the results of completed jobs by their IDs
    private final Map<String, TestExecutionResponse> jobResultMap = new ConcurrentHashMap<>();

    private final JobTrackingService jobTrackingService;

    /**
     * Creates a new job with a unique ID and sets its status to PENDING.
     *
     * @return The unique ID of the created job.
     */
    public String createJob() {
        String jobId = UUID.randomUUID().toString();
        jobStatusMap.put(jobId, JobStatus.PENDING);
        return jobId;
    }

    /**
     * Creates a new job with tracking information and sets its status to PENDING.
     *
     * @param runId     The run ID associated with the job.
     * @param tag       The tag associated with the job.
     * @param createdBy The user who created the job.
     * @return The unique ID of the created job.
     */
    public String createJobWithTracking(String runId, String tag, String createdBy) {
        String jobId = jobTrackingService.startAsyncJob(runId, tag, createdBy);
        jobStatusMap.put(jobId, JobStatus.PENDING);
        return jobId;
    }

    /**
     * Registers a thread for a specific job ID.
     *
     * @param jobId  The ID of the job.
     * @param thread The thread associated with the job.
     */
    public void registerJobThread(String jobId, Thread thread) {
        jobThreadMap.put(jobId, thread);
        log.debug("Registered thread for jobId {}: {}", jobId, thread.getName());
    }

    /**
     * Updates the status of a job to RUNNING and notifies the job tracking service.
     *
     * @param jobId The ID of the job.
     */
    public void setJobRunning(String jobId) {
        jobStatusMap.put(jobId, JobStatus.RUNNING);
        jobTrackingService.updateJobStatus(jobId, JobStatus.RUNNING);
        log.info("Job {} is now RUNNING", jobId);
    }

    /**
     * Updates the status of a job and notifies the job tracking service.
     *
     * @param jobId  The ID of the job.
     * @param status The new status of the job.
     */
    public void updateJobStatus(String jobId, JobStatus status) {
        jobStatusMap.put(jobId, status);
        jobTrackingService.updateJobStatus(jobId, status);
        log.info("Job {} status updated to {}", jobId, status);
    }

    /**
     * Marks a job as COMPLETED, stores its result, and removes its thread.
     *
     * @param jobId    The ID of the job.
     * @param response The result of the completed job.
     */
    public void completeJob(String jobId, TestExecutionResponse response) {
        jobStatusMap.put(jobId, JobStatus.COMPLETED);
        jobResultMap.put(jobId, response);
        jobThreadMap.remove(jobId);
        jobTrackingService.completeJob(jobId, JobStatus.COMPLETED);
        log.info("Job {} COMPLETED successfully", jobId);
    }

    /**
     * Marks a job as FAILED and removes its thread.
     *
     * @param jobId The ID of the job.
     */
    public void failJob(String jobId) {
        jobStatusMap.put(jobId, JobStatus.FAILED);
        jobThreadMap.remove(jobId);
        jobTrackingService.updateJobStatus(jobId, JobStatus.FAILED);
    }

    /**
     * Marks a job as FAILED with an error message and removes its thread.
     *
     * @param jobId       The ID of the job.
     * @param errorMessage The error message associated with the failure.
     */
    public void failJob(String jobId, String errorMessage) {
        jobStatusMap.put(jobId, JobStatus.FAILED);
        jobThreadMap.remove(jobId);
        jobTrackingService.updateJobStatus(jobId, JobStatus.FAILED, errorMessage);
        log.error("Job {} failed: {}", jobId, errorMessage);
    }

    /**
     * Retrieves the status of a job by its ID.
     *
     * @param jobId The ID of the job.
     * @return The status of the job, or null if the job does not exist.
     */
    public JobStatus getStatus(String jobId) {
        return jobStatusMap.getOrDefault(jobId, null);
    }

    /**
     * Retrieves the result of a completed job by its ID.
     *
     * @param jobId The ID of the job.
     * @return The result of the job, or null if the job does not exist or is not completed.
     */
    public TestExecutionResponse getResult(String jobId) {
        return jobResultMap.get(jobId);
    }

    /**
     * Cancels a job by interrupting its thread and updating its status.
     *
     * @param jobId The ID of the job.
     * @return True if the job was successfully canceled, false otherwise.
     */
    public boolean cancelJob(String jobId) {
        Thread thread = jobThreadMap.get(jobId);
        if (thread != null && thread.isAlive()) {
            thread.interrupt(); // Send interrupt signal
            updateJobStatus(jobId, JobStatus.CANCELLED);
            jobThreadMap.remove(jobId);
            return true;
        }
        return jobTrackingService.cancelJob(jobId);
    }

    /**
     * Counts the number of active jobs (RUNNING or PENDING).
     *
     * @return The count of active jobs.
     */
    public int getActiveJobsCount() {
        return (int) jobStatusMap.values().stream()
                .filter(status -> status == JobStatus.RUNNING || status == JobStatus.PENDING)
                .count();
    }

    /**
     * Cleans up completed, failed, or canceled jobs from the job status and result maps.
     */
    public void cleanupCompletedJobs() {
        jobStatusMap.entrySet().removeIf(entry ->
                entry.getValue() == JobStatus.COMPLETED ||
                        entry.getValue() == JobStatus.FAILED ||
                        entry.getValue() == JobStatus.CANCELLED);

        jobResultMap.keySet().removeIf(jobId -> !jobStatusMap.containsKey(jobId));
    }
}