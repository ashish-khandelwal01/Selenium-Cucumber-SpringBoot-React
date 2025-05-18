package com.framework.apiserver.service.impl;

import com.framework.apiserver.dto.RunInfo;
import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.service.TestExecutionService;
import com.framework.apiserver.testrunner.TestRunner;
import com.framework.apiserver.utilities.CommonUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

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
public class TestExecutionServiceImpl implements TestExecutionService {

    @Autowired
    private CommonUtils commonUtils;

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
            String runId = CommonUtils.generateRunId();
            System.out.println("Run ID: " + runId);

            System.setProperty("run.id", runId);
            System.setProperty("cucumber.filter.tags", tag);
            LocalDateTime startTime = LocalDateTime.now();
            Result result = JUnitCore.runClasses(TestRunner.class);
            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = Duration.between(startTime, endTime).getSeconds();
            int failureCount = result.getFailureCount();
            int total = result.getRunCount();
            int passed = total - failureCount;
            System.out.println("Test execution completed with " + failureCount + " failures.");
            String status = failureCount == 0
                    ? "Execution Successful"
                    : "Execution Completed with Failures: " + failureCount;

            RunInfo runInfo = new RunInfo();
            runInfo.setRunId(runId);
            runInfo.setTags(tag);
            runInfo.setStartTime(startTime);
            runInfo.setEndTime(endTime);
            runInfo.setDurationSeconds(durationSeconds);
            runInfo.setTotal(total);
            runInfo.setPassed(passed);
            runInfo.setFailed(failureCount);
            runInfo.setStatus(status);

            String latestReportFolder = commonUtils.getMostRecentReportFolder(".");
            if (latestReportFolder != null) {
                commonUtils.moveReportToRunIdFolder(latestReportFolder, runId);
                commonUtils.moveCucumberReportsToRunIdFolder(runId);
                commonUtils.writeRunInfo(runInfo);
                commonUtils.zipReportFolder(runId);
            }

            return new TestExecutionResponse(status, failureCount, runId);
        } catch (Exception e) {
            return new TestExecutionResponse("Execution Failed: " + e.getMessage(), -1, null);
        }
    }
}