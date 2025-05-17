package com.framework.apiserver.utilities;

import io.cucumber.java.Scenario;
import org.testng.Assert;
import org.testng.ISuiteListener;
import org.testng.ITestListener;

/**
 * BaseClass provides utility methods for logging test information and results.
 * Implements ITestListener and ISuiteListener from TestNG.
 *
 * @see Scenario
 * @see ITestListener
 * @see ISuiteListener
 * @see Assert
 *
 * @author ashish-khandelwal01
 */
public class BaseClass implements ITestListener, ISuiteListener {
    public static Scenario scenario;

    /**
     * Logs an informational message to the scenario and console.
     *
     * @param message The message to log.
     */
    public static void infoLog(String message){
        extentReportLog(scenario, "\t" + message);
        System.out.println(message);
    }

    /**
     * Logs a failure message to the scenario and console, and fails the test.
     *
     * @param message The failure message to log.
     */
    public static void failLog(String message){
        extentReportLog(scenario, "\tFailed: "+ message);
        System.out.println(message);
        Assert.fail(message);
    }

    /**
     * Logs a success message to the scenario and console.
     *
     * @param message The success message to log.
     */
    public static void passLog(String message){
        extentReportLog(scenario, "\t"+ message);
        System.out.println(message);
    }

    /**
     * Logs a message to the scenario with formatting for HTML reports.
     *
     * @param scenario The scenario to log the message to.
     * @param message The message to log.
     */
    public static void extentReportLog(Scenario scenario, String message) {
        try{
            message = message.replace("\t","&emsp;");
            message = message.replace("\n","<br>");
            scenario.log(message);
        }catch(Exception e){
            System.out.println("Test step status is not updated in Extent report");
        }
    }
}