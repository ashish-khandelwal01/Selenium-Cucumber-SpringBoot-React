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
     * @param jobId The ID of the job associated with the rerun.
     * @return A TestExecutionResponse object containing the results of the rerun.
     */
    TestExecutionResponse rerunAll(String runId, String jobId);

    /**
     * Reruns only the failed tests for a specific test run.
     *
     * @param runId The ID of the test run to rerun failed tests for.
     * @param createdBy The ID of the user who started the job.
     * @return A TestExecutionResponse object containing the results of the rerun.
     */
    TestExecutionResponse rerunFailed(String runId, String createdBy);

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