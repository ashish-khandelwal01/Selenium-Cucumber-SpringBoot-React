package com.framework.apiserver.testrunner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

/**
 * TestRunner is the entry point for executing Cucumber tests.
 *
 * <p>This class is annotated with @RunWith to specify that it uses the Cucumber test runner.
 * The @CucumberOptions annotation is used to configure various aspects of the test execution,
 * such as the location of feature files, step definitions, and reporting plugins.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@RunWith: Specifies the test runner to use (Cucumber in this case).</li>
 *   <li>@CucumberOptions: Configures Cucumber-specific options for test execution.</li>
 * </ul>
 *
 * <p>Configuration:</p>
 * <ul>
 *   <li>features: Path to the directory containing Cucumber feature files.</li>
 *   <li>glue: Packages containing step definitions and hooks.</li>
 *   <li>plugin: Plugins for generating reports (e.g., HTML, JSON, ExtentReports).</li>
 *   <li>monochrome: Ensures clean and readable console output.</li>
 *   <li>tags: Filters scenarios to execute based on tags.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Run this class as a JUnit test to execute the Cucumber scenarios.</li>
 *   <li>Ensure the feature files and step definitions are correctly configured.</li>
 * </ul>
 */
@RunWith(Cucumber.class)
@CucumberOptions(
		features = "src/test/resources/features",        // Path to your feature files
		glue = {"com.framework.apiserver.stepDefinitions", "com.framework.apiserver.hooks"}, // Path to your step definitions and hooks
		plugin = {
				"pretty",                                    // Standard console output
				"html:target/cucumber-reports.html",          // HTML report output
				"json:target/cucumber-reports.json",          // JSON for CI tools integration
				"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"  // ExtentReports integration
		},
		monochrome = true,                              // Clean and readable console output
		tags = "@Test"                                  // Filter scenarios by tag
)
public class TestRunner {
}