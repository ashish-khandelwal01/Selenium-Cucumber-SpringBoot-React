package com.framework.apiserver.testrunner;

import io.cucumber.core.cli.Main;
import java.util.ArrayList;
import java.util.List;

/**
 * The TestRunner class is responsible for executing Cucumber tests.
 *
 * <p>This class constructs the necessary Cucumber options, including feature file paths,
 * glue code locations, and reporting plugins. It also supports dynamically passing
 * tags for filtering test scenarios.</p>
 */
public class TestRunner {

	/**
	 * The main method serves as the entry point for running Cucumber tests.
	 *
	 * <p>It performs the following steps:</p>
	 * <ul>
	 *   <li>Retrieves the `run.id` system property to uniquely identify the test run.</li>
	 *   <li>Constructs a list of Cucumber options, including feature file paths, glue code,
	 *       and reporting plugins.</li>
	 *   <li>Optionally adds a tag filter if the `cucumber.filter.tags` system property is set.</li>
	 *   <li>Invokes the Cucumber `Main.run` method to execute the tests with the specified options.</li>
	 * </ul>
	 *
	 * @param args Command-line arguments (not used in this implementation).
	 */
	public static void main(String[] args) {
		// Retrieve the run ID from system properties
		String runId = System.getProperty("run.id");

		// Initialize Cucumber options
		List<String> cucumberOptions = new ArrayList<>(List.of(
				"src/test/resources/features", // Path to feature files
				"--glue", "com.framework.apiserver.stepDefinitions", // Step definitions package
				"--glue", "com.framework.apiserver.hooks", // Hooks package
				"--plugin", "pretty", // Pretty console output
				"--plugin", "html:reports/" + runId + "/cucumber-reports.html", // HTML report
				"--plugin", "json:reports/" + runId + "/cucumber-reports.json", // JSON report
				"--plugin", "com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:", // Extent report
				"--monochrome" // Disable colored output for better readability
		));

		// Optionally add tag filtering if specified
		String tag = System.getProperty("cucumber.filter.tags");
		if (tag != null && !tag.isEmpty()) {
			cucumberOptions.add("--tags");
			cucumberOptions.add(tag);
		}

		// Run Cucumber with the specified options
		Main.run(cucumberOptions.toArray(new String[0]), Thread.currentThread().getContextClassLoader());
	}
}