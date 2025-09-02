package com.framework.apiserver.utilities;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * DriverManager is a utility class responsible for managing WebDriver instances.
 *
 * <p>This class provides methods to retrieve, create, and quit WebDriver instances.
 * It ensures that a single WebDriver instance is reused unless explicitly replaced.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Component: Marks this class as a Spring-managed bean.</li>
 *   <li>@Autowired: Injects the SeleniumTestBase dependency.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Call getDriver() to retrieve the current WebDriver instance or create a new one if none exists.</li>
 *   <li>Call createNewDriver() to create a new WebDriver instance, replacing any existing one.</li>
 *   <li>Call quitDriver() to close and nullify the current WebDriver instance.</li>
 * </ul>
 *
 * @see SeleniumTestBase
 * @see WebDriver
 */
@Component
public class DriverManager {

    private final SeleniumTestBase seleniumTestBase;

    // Holds the current active driver instance
    private WebDriver currentDriver;

    /**
     * Constructs a DriverManager instance with the required SeleniumTestBase dependency.
     *
     * @param seleniumTestBase The SeleniumTestBase instance used for browser setup.
     */
    @Autowired
    public DriverManager(SeleniumTestBase seleniumTestBase) {
        this.seleniumTestBase = seleniumTestBase;
    }

    /**
     * Retrieves the current WebDriver instance.
     * If no instance exists, a new one is created using SeleniumTestBase.
     *
     * @return The current WebDriver instance.
     */
    public WebDriver getDriver() {
        if (currentDriver == null) {
            currentDriver = seleniumTestBase.browserSetup();
            seleniumTestBase.setDriver(currentDriver);
        }
        return currentDriver;
    }

    /**
     * Creates a new WebDriver instance, replacing any existing one.
     * If an existing instance is present, it is closed before creating a new one.
     *
     * @return The newly created WebDriver instance.
     */
    public WebDriver createNewDriver() {
        if (currentDriver != null) {
            try {
                currentDriver.quit();
            } catch (Exception ignored) {}
        }
        currentDriver = seleniumTestBase.browserSetup();
        seleniumTestBase.setDriver(currentDriver);
        return currentDriver;
    }

    /**
     * Closes the current WebDriver instance and sets it to null.
     * Ensures proper cleanup of resources.
     */
    public void quitDriver() {
        if (currentDriver != null) {
            try {
                currentDriver.quit();
            } catch (Exception ignored) {}
            currentDriver = null;
        }
    }
}