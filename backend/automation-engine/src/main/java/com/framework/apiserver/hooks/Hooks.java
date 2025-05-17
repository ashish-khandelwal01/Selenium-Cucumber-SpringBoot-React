package com.framework.apiserver.hooks;

import com.framework.apiserver.utilities.BaseClass;
import com.framework.apiserver.utilities.DriverManager;
import com.framework.apiserver.utilities.SeleniumTestBase;
import io.cucumber.java.*;
import org.springframework.beans.factory.annotation.Autowired;

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
public class Hooks {

    private final DriverManager driverManager;

    @Autowired
    private BaseClass baseClass;

    @Autowired
    private SeleniumTestBase seleniumTestBase;

    /**
     * Constructs a Hooks instance with the required DriverManager dependency.
     *
     * @param driverManager The DriverManager instance responsible for managing the WebDriver.
     */
    @Autowired
    public Hooks(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

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