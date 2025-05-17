package com.framework.apiserver.hooks;

import com.framework.apiserver.ApiServerApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * CucumberSpringConfiguration is a configuration class that integrates
 * Cucumber with the Spring Boot testing framework.
 *
 * <p>It ensures that the Spring application context is loaded and shared
 * across Cucumber step definitions during test execution.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@CucumberContextConfiguration: Indicates that this class provides
 *       Cucumber-specific context configuration.</li>
 *   <li>@SpringBootTest: Loads the Spring Boot application context for testing,
 *       using the specified application class.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>This class is automatically picked up by Cucumber to initialize
 *       the Spring context before executing tests.</li>
 * </ul>
 */
@CucumberContextConfiguration
@SpringBootTest(classes = ApiServerApplication.class)
public class CucumberSpringConfiguration {
}