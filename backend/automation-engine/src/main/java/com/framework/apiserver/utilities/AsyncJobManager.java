package com.framework.apiserver.utilities;

import com.framework.apiserver.config.JobStatus;
import com.framework.apiserver.dto.TestExecutionResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AsyncJobManager {

    private final Map<String, JobStatus> jobStatusMap = new ConcurrentHashMap<>();
    private final Map<String, TestExecutionResponse> jobResultMap = new ConcurrentHashMap<>();

    public String createJob() {
        String jobId = UUID.randomUUID().toString();
        jobStatusMap.put(jobId, JobStatus.PENDING);
        return jobId;
    }

    public void setJobRunning(String jobId) {
        jobStatusMap.put(jobId, JobStatus.RUNNING);
    }

    public void completeJob(String jobId, TestExecutionResponse response) {
        jobStatusMap.put(jobId, JobStatus.COMPLETED);
        jobResultMap.put(jobId, response);
    }

    public void failJob(String jobId) {
        jobStatusMap.put(jobId, JobStatus.FAILED);
    }

    public JobStatus getStatus(String jobId) {
        return jobStatusMap.getOrDefault(jobId, null);
    }

    public TestExecutionResponse getResult(String jobId) {
        return jobResultMap.get(jobId);
    }
}