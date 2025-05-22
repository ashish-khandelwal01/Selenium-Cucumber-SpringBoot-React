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

}