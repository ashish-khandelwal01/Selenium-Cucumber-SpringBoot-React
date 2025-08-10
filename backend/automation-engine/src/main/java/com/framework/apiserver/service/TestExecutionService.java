package com.framework.apiserver.service;

import com.framework.apiserver.dto.TestExecutionResponse;

/**
 * Service interface for managing test execution operations.
 */
public interface TestExecutionService {

    /**
     * Executes Cucumber tests based on the specified tag.
     *
     * @param tag The tag used to filter and execute specific Cucumber tests.
     * @return A TestExecutionResponse object containing the results of the test execution.
     */
    TestExecutionResponse runCucumberTests(String tag);

    /**
     * Executes tests asynchronously based on the specified tag and associates the execution with a job ID.
     *
     * @param tag The tag used to filter and execute specific tests.
     * @param jobId The unique identifier for the asynchronous test execution job.
     */
    void runTestsAsync(String tag, String jobId);
}