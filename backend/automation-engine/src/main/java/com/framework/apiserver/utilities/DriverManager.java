package com.framework.apiserver.utilities;

import com.framework.apiserver.service.BrowserContextManager;
import lombok.Getter;
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

    @Autowired
    private BrowserContextManager browserContextManager;

    /**
     * -- GETTER --
     *  Gets the browser type that the current driver was created for.
     *
     * @return The current browser type, or null if no driver exists.
     */
    // Track which browser type the current driver was created for
    @Getter
    private String currentBrowserType;

    /**
     * Constructs a DriverManager instance with the required SeleniumTestBase dependency.
     */
    @Autowired
    public DriverManager(SeleniumTestBase seleniumTestBase) {
        this.seleniumTestBase = seleniumTestBase;
    }

    /**
     * Retrieves the current WebDriver instance.
     * If no instance exists or browser type has changed, a new one is created.
     *
     * @return The current WebDriver instance.
     */
    public synchronized WebDriver getDriver() {
        String requestedBrowserType = System.getProperty("browserName");
        // Create new driver if none exists or browser type changed
        if (currentDriver == null || !requestedBrowserType.equals(currentBrowserType)) {
            if (currentDriver != null) {
                try {
                    currentDriver.quit();
                } catch (Exception e) {
                    System.out.println("Error closing previous driver: " + e.getMessage());
                }
                currentDriver = null;
            }

            currentDriver = seleniumTestBase.browserSetup(requestedBrowserType);
            seleniumTestBase.setDriver(currentDriver);
            currentBrowserType = requestedBrowserType;
        } else {
            System.out.println("DriverManager: Reusing existing " + currentBrowserType + " WebDriver instance");
        }

        return currentDriver;
    }

    /**
     * Creates a new WebDriver instance, replacing any existing one.
     * If an existing instance is present, it is closed before creating a new one.
     *
     * @return The newly created WebDriver instance.
     */
    public synchronized WebDriver createNewDriver() {
        if (currentDriver != null) {
            try {
                currentDriver.quit();
            } catch (Exception e) {
                System.out.println("Error closing existing driver: " + e.getMessage());
            }
        }

        String browserType = System.getProperty("browserName");
        currentDriver = seleniumTestBase.browserSetup(browserType);
        seleniumTestBase.setDriver(currentDriver);
        currentBrowserType = browserType;
        return currentDriver;
    }

    /**
     * Closes the current WebDriver instance and sets it to null.
     * Ensures proper cleanup of resources.
     */
    public synchronized void quitDriver() {
        if (currentDriver != null) {
            try {
                currentDriver.quit();
            } catch (Exception e) {
                System.out.println("Error quitting driver: " + e.getMessage());
            }
            currentDriver = null;
            currentBrowserType = null;
        }
    }

}