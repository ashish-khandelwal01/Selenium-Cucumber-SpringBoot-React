package com.framework.apiserver.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.framework.apiserver.dto.RunInfo;
import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.service.TestExecutionService;
import com.framework.apiserver.service.TestRerunService;
import com.framework.apiserver.testrunner.TestFailedRunner;
import com.framework.apiserver.testrunner.TestRunner;
import com.framework.apiserver.utilities.CommonUtils;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of the TestRerunService interface.
 *
 * <p>This service provides methods to rerun all tests or only the failed tests
 * for a specific test run. It interacts with the test execution service and
 * utilities to manage test reruns and report generation.</p>
 */
@Service
public class TestRerunServiceImpl implements TestRerunService {

    @Autowired
    private TestExecutionService testExecutionService;

    @Autowired
    private CommonUtils commonUtils;

    private static final String REPORTS_BASE_PATH = "reports";

    /**
     * Reruns all tests for the specified run ID.
     *
     * <p>This method retrieves the tags associated with the given run ID from the
     * `run-info.json` file and triggers the execution of all tests using those tags.</p>
     *
     * @param runId The unique identifier of the test run to rerun.
     * @return A TestExecutionResponse object containing the results of the rerun.
     */
    @Override
    public TestExecutionResponse rerunAll(String runId) {
        File infoFile = new File(REPORTS_BASE_PATH + "/" + runId + "/run-info.json");
        if (!infoFile.exists()) {
            return new TestExecutionResponse("Run ID not found: " + runId, -1, runId);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(infoFile);
            String tags = node.get("tags").asText();
            return testExecutionService.runCucumberTests(tags);
        } catch (Exception e) {
            return new TestExecutionResponse("Failed to read run-info.json: " + e.getMessage(), -1, null);
        }
    }

    /**
     * Extracts the paths and line numbers of failed scenarios from the cucumber report.
     *
     * <p>This method reads the `cucumber-report.json` file for the specified run ID
     * and identifies scenarios that failed. It constructs a list of paths with line
     * numbers for rerunning the failed scenarios.</p>
     *
     * @param runId The unique identifier of the test run.
     * @return A list of strings representing the paths and line numbers of failed scenarios.
     */
    public List<String> extractFailedScenarioPathsWithLineNumbers(String runId) {
        List<String> rerunList = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        Path cucumberJsonPath = Path.of(REPORTS_BASE_PATH, runId, "cucumber-reports.json");

        try {
            JsonNode root = mapper.readTree(cucumberJsonPath.toFile());
            for (JsonNode feature : root) {
                String uri = feature.get("uri").asText();
                for (JsonNode element : feature.get("elements")) {
                    if (element.get("type").asText().equals("scenario")) {
                        boolean hasFailure = false;
                        for (JsonNode step : element.get("steps")) {
                            String result = step.get("result").get("status").asText();
                            if ("failed".equals(result)) {
                                hasFailure = true;
                                break;
                            }
                        }
                        if (hasFailure) {
                            int line = element.get("line").asInt();
                            // Construct path relative to your test resources
                            rerunList.add(uri + ":" + line);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rerunList;
    }

    /**
     * Reruns only the failed tests for the specified run ID.
     *
     * <p>This method identifies failed scenarios from the cucumber report for the given run ID
     * and triggers their execution. It also updates the test reports and generates a new run ID
     * for the rerun.</p>
     *
     * @param runId The unique identifier of the test run to rerun failed tests.
     * @return A TestExecutionResponse object containing the results of the rerun.
     */
    @Override
    public TestExecutionResponse rerunFailed(String runId) {
        List<String> failedScenarioPathsWithLines = extractFailedScenarioPathsWithLineNumbers(runId);
        if (failedScenarioPathsWithLines.isEmpty()) {
            return new TestExecutionResponse("No failed scenarios found for runId " + runId, 0, null);
        }

        try {
            String newRunId = CommonUtils.generateRunId();
            System.setProperty("run.id", newRunId);

            LocalDateTime startTime = LocalDateTime.now();
            Path rerunFilePath = Paths.get("target/rerun.txt");
            Files.write(rerunFilePath, failedScenarioPathsWithLines);
            Result result = JUnitCore.runClasses(TestFailedRunner.class);
            commonUtils.deleteFile(rerunFilePath.toString());
            int failureCount = result.getFailureCount();
            int total = result.getRunCount();
            int passed = total - failureCount;

            String status = failureCount == 0
                    ? "Rerun Successful"
                    : "Rerun Completed with Failures: " + failureCount;

            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = Duration.between(startTime, endTime).getSeconds();
            System.out.println("Test execution completed with " + failureCount + " failures.");

            RunInfo runInfo = new RunInfo();
            runInfo.setRunId(newRunId);
            runInfo.setTags(String.valueOf(failedScenarioPathsWithLines));
            runInfo.setStartTime(startTime);
            runInfo.setEndTime(endTime);
            runInfo.setDurationSeconds(durationSeconds);
            runInfo.setTotal(total);
            runInfo.setPassed(passed);
            runInfo.setFailed(failureCount);
            runInfo.setStatus(status);

            String latestReportFolder = commonUtils.getMostRecentReportFolder(".");
            if (latestReportFolder != null) {
                commonUtils.moveReportToRunIdFolder(latestReportFolder, newRunId);
                commonUtils.moveCucumberReportsToRunIdFolder(newRunId);
                commonUtils.writeRunInfo(runInfo);
                commonUtils.zipReportFolder(newRunId);
            }

            return new TestExecutionResponse(status, failureCount, newRunId);

        } catch (Exception e) {
            return new TestExecutionResponse("Rerun Failed: " + e.getMessage(), -1,null);
        }
    }
}