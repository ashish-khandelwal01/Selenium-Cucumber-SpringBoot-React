package com.framework.apiserver.service.impl;

import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.service.BrowserContextManager;
import com.framework.apiserver.service.JobTrackingService;
import com.framework.apiserver.service.TestExecutionService;
import com.framework.apiserver.service.TestRunInfoService;
import com.framework.apiserver.utilities.AsyncJobManager;
import com.framework.apiserver.utilities.CommonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * Implementation of the TestExecutionService interface.
 *
 * <p>This service is responsible for executing Cucumber tests, processing the results,
 * and managing asynchronous test execution jobs.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Service: Marks this class as a Spring service component.</li>
 *   <li>@Async: Indicates that the method should be executed asynchronously.</li>
 * </ul>
 *
 * @see TestExecutionService
 * @see TestExecutionResponse
 */
@Service
public class TestExecutionServiceImpl implements TestExecutionService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private AsyncJobManager asyncJobManager;

    @Autowired
    private JobTrackingService jobTrackingService;

    @Autowired
    BrowserContextManager browserContextManager;

    @Autowired
    private TestRunInfoService testRunInfoService;

    /**
     * Executes Cucumber tests filtered by the specified tag.
     *
     * <p>This method sets system properties for Cucumber tag filtering, runs the tests
     * using JUnitCore, and processes the results to generate a summary of the execution.</p>
     *
     * @param tag       The Cucumber tag used to filter the tests to be executed.
     * @param jobId  The unique identifier for the asynchronous test execution job.
     * @return A TestExecutionResponse object containing the execution status,
     * the number of test failures, and the run ID.
     */
    public TestExecutionResponse runCucumberTests(String tag, String jobId, boolean isAsync) {
        String runId = CommonUtils.generateRunId();
        System.out.println("Run ID: " + runId);
        LocalDateTime startTime = LocalDateTime.now();
        if(!isAsync) {
            asyncJobManager.setJobRunning(jobId);
        }
        try {
            // Command to launch a new JVM process
            CommonUtils.testCaseRun(tag, runId, Path.of("."), browserContextManager.getBrowserType());

            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = Duration.between(startTime, endTime).getSeconds();

            HashMap<String, Object> result = commonUtils.createRunInfoFileAndDb(testRunInfoService, tag, runId, startTime, endTime,
                    durationSeconds);
            TestExecutionResponse response = new TestExecutionResponse(
                    String.valueOf(result.get("status")),
                    (Integer) result.get("failureCount"),
                    runId
            );
            if(!isAsync) {
                asyncJobManager.completeJob(jobId, response);
            }
            return response;
        } catch (Exception e) {
            asyncJobManager.failJob(jobId, e.getMessage());
            return new TestExecutionResponse("Execution Failed: " + e.getMessage(), -1, null);
        }
    }

    /**
     * Executes Cucumber tests asynchronously based on the specified tag and created by.
     *
     * <p>This method updates the job status to "running", executes the tests,
     * and updates the job status to "completed" or "failed" based on the result.</p>
     *
     * @param tag   The Cucumber tag used to filter the tests to be executed.
     * @param createdBy The ID of the user who started the job.
     */
    public String runTestsAsync(String tag, String createdBy) {
        String jobId = asyncJobManager.createJobWithTracking(null, tag, createdBy);
        Thread jobThread = new Thread(() -> {
            asyncJobManager.setJobRunning(jobId);
            try {
                TestExecutionResponse response = runCucumberTests(tag, jobId, true);
                asyncJobManager.completeJob(jobId, response);
            } catch (Exception e) {
                asyncJobManager.failJob(jobId, e.getMessage());
            }
        });

        jobThread.setName("AsyncTest-" + tag + "-" + jobId.substring(0, 8));
        asyncJobManager.registerJobThread(jobId, jobThread);
        jobThread.start();

        return jobId;
    }

    /**
     * Executes Cucumber tests asynchronously based on the specified tag and job ID.
     *
     * <p>This method updates the job status to "running", executes the tests,
     * and updates the job status to "completed" or "failed" based on the result.</p>
     *
     * @param tag   The Cucumber tag used to filter the tests to be executed.
     * @param jobId The unique identifier of the asynchronous job.
     */
    @Deprecated
    public void runTestsAsyncLegacy(String tag, String jobId) {
        Thread jobThread = new Thread(() -> {
            asyncJobManager.setJobRunning(jobId);
            try {
                TestExecutionResponse response = runCucumberTests(tag, jobId, true);
                asyncJobManager.completeJob(jobId, response);
            } catch (Exception e) {
                asyncJobManager.failJob(jobId);
            }
        });
        jobThread.setName("AsyncTest-" + tag + "-" + jobId.substring(0, 8));
        asyncJobManager.registerJobThread(jobId, jobThread);
        jobThread.start();
    }
}