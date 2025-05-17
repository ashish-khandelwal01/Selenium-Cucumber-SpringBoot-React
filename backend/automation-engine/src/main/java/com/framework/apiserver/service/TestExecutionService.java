package com.framework.apiserver.service;

import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.testrunner.TestRunner;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.stereotype.Service;

/**
 * TestExecutionService is a service class responsible for executing Cucumber tests
 * and returning the results of the test execution.
 *
 * <p>It uses JUnitCore to run the tests and processes the results to provide
 * a summary of the execution status and failure count.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Service: Indicates that this class is a Spring service component.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Call the runCucumberTests method with a specific tag to execute tests
 *       filtered by that tag.</li>
 * </ul>
 *
 * @see TestExecutionResponse
 * @see TestRunner
 */
@Service
public class TestExecutionService {

    /**
     * Executes Cucumber tests filtered by the specified tag.
     *
     * <p>Sets the system property for Cucumber tag filtering, runs the tests
     * using JUnitCore, and processes the results to determine the execution status.</p>
     *
     * @param tag The Cucumber tag used to filter the tests to be executed.
     * @return A TestExecutionResponse object containing the execution status
     *         and the number of test failures.
     */
    public TestExecutionResponse runCucumberTests(String tag) {
        try {
            System.setProperty("cucumber.filter.tags", tag);
            Result result = JUnitCore.runClasses(TestRunner.class);
            int failureCount = result.getFailureCount();
            System.out.println("Test execution completed with " + failureCount + " failures.");
            String status = failureCount == 0
                    ? "Execution Successful"
                    : "Execution Completed with Failures: " + failureCount;

            return new TestExecutionResponse(status, failureCount);
        } catch (Exception e) {
            return new TestExecutionResponse("Execution Failed: " + e.getMessage(), -1);
        }
    }
}