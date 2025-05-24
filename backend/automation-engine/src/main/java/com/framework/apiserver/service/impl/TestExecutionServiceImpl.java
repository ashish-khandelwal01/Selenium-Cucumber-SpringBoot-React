package com.framework.apiserver.service.impl;

import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.service.TestExecutionService;
import com.framework.apiserver.service.TestRunInfoService;
import com.framework.apiserver.testrunner.TestRunner;
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
 * @see TestRunner
 */
@Service
public class TestExecutionServiceImpl implements TestExecutionService {

    @Autowired
    private CommonUtils commonUtils;

    @Autowired
    private AsyncJobManager asyncJobManager;

    @Autowired
    private TestRunInfoService testRunInfoService;

    /**
     * Executes Cucumber tests filtered by the specified tag.
     *
     * <p>This method sets system properties for Cucumber tag filtering, runs the tests
     * using JUnitCore, and processes the results to generate a summary of the execution.</p>
     *
     * @param tag The Cucumber tag used to filter the tests to be executed.
     * @return A TestExecutionResponse object containing the execution status,
     *         the number of test failures, and the run ID.
     */
    public TestExecutionResponse runCucumberTests(String tag) {
        String runId = CommonUtils.generateRunId();
        System.out.println("Run ID: " + runId);
        LocalDateTime startTime = LocalDateTime.now();
        try {
            // Command to launch a new JVM process
            CommonUtils.testCaseRun(tag, runId, Path.of("."));
            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = Duration.between(startTime, endTime).getSeconds();

            HashMap<String, Object> result = commonUtils.createRunInfoFileAndDb(testRunInfoService, tag, runId, startTime, endTime,
                    durationSeconds);

            return new TestExecutionResponse(String.valueOf(result.get("status")),
                    (Integer) result.get("failureCount"), runId);
        } catch (Exception e) {
            return new TestExecutionResponse("Execution Failed: " + e.getMessage(), -1, null);
        }
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
    public void runTestsAsync(String tag, String jobId) {
        Thread jobThread = new Thread(() -> {
            asyncJobManager.setJobRunning(jobId);
            try {
                TestExecutionResponse response = runCucumberTests(tag);
                asyncJobManager.completeJob(jobId, response);
            } catch (Exception e) {
                asyncJobManager.failJob(jobId);
            }
        });
        asyncJobManager.registerJobThread(jobId, jobThread);
        jobThread.start();
    }
}