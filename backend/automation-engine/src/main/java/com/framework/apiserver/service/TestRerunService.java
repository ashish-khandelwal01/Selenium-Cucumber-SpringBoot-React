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
     * @param createdBy The ID of the user who started the job.
     */
    String rerunTestsAsync(String runId, String createdBy);

    /**
     * Reruns only the failed tests for a specific test run asynchronously.
     *
     * @param runId The ID of the test run to rerun failed tests for.
     * @param createdBy The ID of the user who started the job.
     */
    String rerunFailedTestsAsync(String runId, String createdBy);
}