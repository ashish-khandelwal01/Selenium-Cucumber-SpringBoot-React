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

@Component
@RequiredArgsConstructor
@Slf4j
public class AsyncJobManager {

    private final Map<String, JobStatus> jobStatusMap = new ConcurrentHashMap<>();
    private final Map<String, Thread> jobThreadMap = new ConcurrentHashMap<>();
    private final Map<String, TestExecutionResponse> jobResultMap = new ConcurrentHashMap<>();

    private final JobTrackingService jobTrackingService;

    public String createJob() {
        String jobId = UUID.randomUUID().toString();
        jobStatusMap.put(jobId, JobStatus.PENDING);
        return jobId;
    }

    public String createJobWithTracking(String runId, String tag, String createdBy) {
        String jobId = jobTrackingService.startAsyncJob(runId, tag, createdBy);
        jobStatusMap.put(jobId, JobStatus.PENDING);
        return jobId;
    }

    public void registerJobThread(String jobId, Thread thread) {
        jobThreadMap.put(jobId, thread);
        log.debug("Registered thread for jobId {}: {}", jobId, thread.getName());
    }

    public void setJobRunning(String jobId) {
        jobStatusMap.put(jobId, JobStatus.RUNNING);
        jobTrackingService.updateJobStatus(jobId, JobStatus.RUNNING);
        log.info("Job {} is now RUNNING", jobId);
    }

    public void updateJobStatus(String jobId, JobStatus status) {
        jobStatusMap.put(jobId, status);
        jobTrackingService.updateJobStatus(jobId, status);
        log.info("Job {} status updated to {}", jobId, status);
    }

    public void completeJob(String jobId, TestExecutionResponse response) {
        jobStatusMap.put(jobId, JobStatus.COMPLETED);
        jobResultMap.put(jobId, response);
        jobThreadMap.remove(jobId);
        jobTrackingService.completeJob(jobId, JobStatus.COMPLETED);
        log.info("Job {} COMPLETED successfully", jobId);
    }

    public void failJob(String jobId) {
        jobStatusMap.put(jobId, JobStatus.FAILED);
        jobThreadMap.remove(jobId);
        jobTrackingService.updateJobStatus(jobId, JobStatus.FAILED);
    }

    public void failJob(String jobId, String errorMessage) {
        jobStatusMap.put(jobId, JobStatus.FAILED);
        jobThreadMap.remove(jobId);

        // Update job tracking with error message
        jobTrackingService.updateJobStatus(jobId, JobStatus.FAILED, errorMessage);
        log.error("Job {} failed: {}", jobId, errorMessage);
    }

    public JobStatus getStatus(String jobId) {
        return jobStatusMap.getOrDefault(jobId, null);
    }

    public TestExecutionResponse getResult(String jobId) {
        return jobResultMap.get(jobId);
    }

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

    public int getActiveJobsCount() {
        return (int) jobStatusMap.values().stream()
                .filter(status -> status == JobStatus.RUNNING || status == JobStatus.PENDING)
                .count();
    }

    public void cleanupCompletedJobs() {
        jobStatusMap.entrySet().removeIf(entry ->
                entry.getValue() == JobStatus.COMPLETED ||
                        entry.getValue() == JobStatus.FAILED ||
                        entry.getValue() == JobStatus.CANCELLED);

        jobResultMap.keySet().removeIf(jobId -> !jobStatusMap.containsKey(jobId));
    }
}