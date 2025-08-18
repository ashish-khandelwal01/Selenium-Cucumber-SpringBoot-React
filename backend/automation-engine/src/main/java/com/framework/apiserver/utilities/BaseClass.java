package com.framework.apiserver.utilities;

import io.cucumber.java.Scenario;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.testng.Assert;
import org.testng.ISuiteListener;
import org.testng.ITestListener;

/**
 * BaseClass provides logging utilities for Cucumber Scenarios and TestNG.
 *
 * <p>This class is a Spring-managed bean, allowing better integration with the framework.
 * It implements TestNG listeners for suite and test-level events, and provides methods
 * for logging informational, success, and failure messages to both the console and
 * Cucumber reports.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Component: Marks this class as a Spring-managed bean.</li>
 *   <li>@Getter, @Setter: Lombok annotations to generate getter and setter methods.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Inject this class into other Spring components to use its logging utilities.</li>
 *   <li>Use the provided methods to log messages during test execution.</li>
 * </ul>
 *
 * @see Scenario
 * @see ITestListener
 * @see ISuiteListener
 * @see Assert
 *
 * @author  ashish-khandelwal01
 */
@Getter
@Setter
@Component
public class BaseClass implements ITestListener, ISuiteListener {

    /**
     * The Cucumber Scenario object used for logging test steps and results.
     * This is set dynamically during test execution.
     */
    protected Scenario scenario;

    /**
     * Logs an informational message to the Cucumber scenario and the console.
     *
     * @param message The informational message to log.
     */
    public void infoLog(String message) {
        extentReportLog("\t" + message);
    }

    /**
     * Logs a failure message to the Cucumber scenario and the console,
     * and marks the test as failed.
     *
     * @param message The failure message to log.
     */
    public void failLog(String message) {
        extentReportLog("\tFailed: " + message);
        Assert.fail(message);
    }

    /**
     * Logs a success message to the Cucumber scenario and the console.
     *
     * @param message The success message to log.
     */
    public void passLog(String message) {
        extentReportLog("\t" + message);
    }

    /**
     * Logs a message to the Cucumber scenario with formatting for HTML reports.
     * If the scenario is null, logs a warning to the console.
     *
     * @param message The message to log, with support for HTML formatting.
     */
    public void extentReportLog(String message) {
        try {
            if (scenario != null) {
                message = message.replace("\t", "&emsp;");
                message = message.replace("\n", "<br>");
                scenario.log(message);
            } else {
                infoLog("Scenario is null. Could not log to report.");
            }
        } catch (Exception e) {
            infoLog("Test step status is not updated in Extent report");
        }
    }
}