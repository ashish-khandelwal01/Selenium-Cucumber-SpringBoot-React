package com.framework.apiserver.testrunner;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
		features = "src/test/resources/features",        // Path to your feature files
		glue = {"com.framework.apiserver.stepDefinitions", "com.framework.apiserver.hooks"},             // Path to your step definitions and hooks
		plugin = {
				"pretty",                                    // Standard console output
				"html:target/cucumber-reports.html",          // HTML report output
				"json:target/cucumber-reports.json",          // JSON for CI tools integration
				"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"  // ExtentReports integration
		},
		monochrome = true,                              // Clean and readable console output
		tags = "@Test"
)

public class TestRunner{

}

