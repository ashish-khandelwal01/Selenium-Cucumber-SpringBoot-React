package com.framework.apiserver.hooks;

import io.cucumber.java.*;
import com.framework.apiserver.utilities.BaseClass;
import com.framework.apiserver.utilities.SeleniumTestBase;


import static com.framework.apiserver.utilities.SeleniumTestBase.driver;

/**
 * The Hooks class contains Cucumber hooks that are executed before and after specific steps or scenarios.
 * It provides functionality to set up the scenario context and capture screenshots on test failures.
 */
public class Hooks extends BaseClass {

    /**
     * Executes before each scenario to set the current scenario context.
     * @param scenario The current Cucumber scenario being executed.
     */
    @Before
    public void beforeHook(Scenario scenario) {
        BaseClass.scenario = scenario;
    }

    /**
     * Closes the browser after each test.
     * This hook runs only for scenarios tagged with @BookStoreDemo.
     */
    @After
    public void tearDown() {
        if(driver != null) {
            SeleniumTestBase sel = new SeleniumTestBase();
            sel.closeBrowser();
        }
    }

    /**
     * Captures a screenshot after each step if the scenario has failed.
     * Ensures the WebDriver instance is valid before attempting to capture the screenshot.
     * @param scenario The current Cucumber scenario being executed.
     */
    @AfterStep
    public void addScreenshot(Scenario scenario) {
        if (scenario.isFailed() && !driver.toString().contains("null")) {
            try {
                SeleniumTestBase sel = new SeleniumTestBase();
                sel.captureScreenshot(scenario);
            } catch (Exception e) {
                failLog("Unable to capture screenshot: " + e.getMessage());
            }
        }
    }
}