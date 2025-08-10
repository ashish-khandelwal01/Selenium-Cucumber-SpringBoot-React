package com.framework.apiserver.service;

import com.framework.apiserver.dto.TestExecutionResponse;

/**
 * Service interface for managing test rerun operations.
 */
public interface TestRerunService {

    /**
     * Reruns all tests for a specific test run.
     *
     * @param runId The ID of the test run to rerun.
     * @return A TestExecutionResponse object containing the results of the rerun.
     */
    TestExecutionResponse rerunAll(String runId);

    /**
     * Reruns only the failed tests for a specific test run.
     *
     * @param runId The ID of the test run to rerun failed tests for.
     * @return A TestExecutionResponse object containing the results of the rerun.
     */
    TestExecutionResponse rerunFailed(String runId);

    /**
     * Reruns all tests for a specific test run asynchronously.
     *
     * @param runId The ID of the test run to rerun.
     * @param jobId The unique identifier for the asynchronous rerun job.
     */
    void rerunTestsAsync(String runId, String jobId);

    /**
     * Reruns only the failed tests for a specific test run asynchronously.
     *
     * @param runId The ID of the test run to rerun failed tests for.
     * @param jobId The unique identifier for the asynchronous rerun job.
     */
    void rerunFailedTestsAsync(String runId, String jobId);
}