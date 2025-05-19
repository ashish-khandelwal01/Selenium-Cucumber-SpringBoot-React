package com.framework.apiserver.hooks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.apiserver.entity.TestRunInfoEntity;
import com.framework.apiserver.service.TestRunInfoService;
import com.framework.apiserver.utilities.BaseClass;
import com.framework.apiserver.utilities.CommonUtils;
import com.framework.apiserver.utilities.DriverManager;
import com.framework.apiserver.utilities.SeleniumTestBase;
import io.cucumber.java.*;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.framework.apiserver.config.SpringContext;
import org.springframework.test.context.ContextConfiguration;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Hooks class provides Cucumber hooks for managing test execution lifecycle.
 *
 * <p>It includes methods to set up preconditions, clean up after scenarios,
 * and handle additional steps like capturing screenshots on failure.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Before: Executes before each scenario to set up preconditions.</li>
 *   <li>@After: Executes after each scenario to clean up resources.</li>
 *   <li>@AfterStep: Executes after each step to perform additional actions like capturing screenshots.</li>
 *   <li>@AfterAll: Executes after all scenarios to process test run information.</li>
 *   <li>@Autowired: Injects Spring-managed dependencies.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Automatically invoked by Cucumber during test execution.</li>
 *   <li>Handles WebDriver lifecycle and scenario-specific actions.</li>
 * </ul>
 *
 * @see BaseClass
 * @see DriverManager
 * @see SeleniumTestBase
 */

@RequiredArgsConstructor
public class Hooks {

    private final DriverManager driverManager;

    @Autowired
    private BaseClass baseClass;

    @Autowired
    private SeleniumTestBase seleniumTestBase;

    @Autowired
    private CommonUtils commonUtils;
    @Autowired
    private static TestRunInfoService testRunInfoService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Executes before each scenario to set up preconditions.
     *
     * @param scenario The current Cucumber scenario being executed.
     */
    @Before
    public void beforeHook(Scenario scenario) {
        baseClass.setScenario(scenario);
    }

    /**
     * Executes after each scenario to clean up resources.
     *
     * <p>Quits the WebDriver instance to ensure proper cleanup.</p>
     */
    @After
    public void tearDown() {
        driverManager.quitDriver();  // quit driver after scenario
    }

    /**
     * Executes after each step to capture a screenshot if the step fails.
     *
     * @param scenario The current Cucumber scenario being executed.
     */
    @AfterStep
    public void addScreenshot(Scenario scenario) {
        try {
            if (scenario.isFailed() && seleniumTestBase.getDriver() != null) {
                seleniumTestBase.captureScreenshot(scenario);
            }
        } catch (Exception e) {
            baseClass.failLog("Unable to capture screenshot: " + e.getMessage());
        }
    }

    /**
     * Executes after all scenarios to process test run information.
     *
     * <p>Reads the `run-info.json` file from the most recent report folder,
     * parses its content, and saves the test run information to the database.</p>
     */
    @AfterAll
    public static void afterAll() {
        String reportsDir = "reports";
        // Look for most recently created run folder (optional logic)
        TestRunInfoService testRunInfoService = SpringContext.getBean(TestRunInfoService.class);
        CommonUtils commonUtils = SpringContext.getBean(CommonUtils.class);

        File latestRunDir = new File(commonUtils.getMostRecentReportFolder(reportsDir));

        File runInfoFile = new File(latestRunDir, "run-info.json");
        if (!runInfoFile.exists()) {
            System.err.println("run-info.json not found in " + latestRunDir.getName());
            return;
        }
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(runInfoFile);
            TestRunInfoEntity runInfo = new TestRunInfoEntity();

            runInfo.setRunId(node.path("runId").asText());
            runInfo.setStartTime(LocalDateTime.parse(node.path("Start Time").asText()));
            runInfo.setEndTime(LocalDateTime.parse(node.path("End Time").asText()));
            runInfo.setDurationSeconds(node.path("Duration in Seconds").asInt());
            runInfo.setTotal(node.path("Total").asInt());
            runInfo.setPassed(node.path("Passed").asInt());
            runInfo.setFailed(node.path("Failed").asInt());
            runInfo.setStatus(node.path("status").asText());

            // Parse the "failures" array if it exists
            List<String> failures = commonUtils.extractFailedScenarioPathsWithLineNumbers(reportsDir, node.path("runId").asText());
            runInfo.setFailureScenarios(failures);
            testRunInfoService.save(runInfo);

            System.out.println("✅ run-info.json imported to DB successfully.");

        } catch (Exception e) {
            System.err.println("❌ Failed to parse or insert run-info.json into DB.");
        }
    }
}