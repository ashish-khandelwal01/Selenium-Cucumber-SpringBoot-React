package com.framework.apiserver.config;

import com.framework.apiserver.utilities.DriverManager;
import org.openqa.selenium.WebDriver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;

/**
 * WebDriverConfig is a configuration class for setting up the WebDriver bean.
 *
 * <p>It provides a WebDriver instance managed by Spring, leveraging the DriverManager utility.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Configuration: Marks this class as a Spring configuration component.</li>
 *   <li>@Bean: Indicates that the webDriver method produces a Spring-managed bean.</li>
 *   <li>@Lazy: Ensures the WebDriver bean is initialized lazily.</li>
 *   <li>@Scope: Defines the scope of the WebDriver bean as "prototype".</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Inject the WebDriver bean into other Spring components to access the WebDriver instance.</li>
 *   <li>The DriverManager utility ensures a singleton WebDriver instance is returned.</li>
 * </ul>
 *
 * @see DriverManager
 * @see WebDriver
 */
@Configuration
public class WebDriverConfig {

    private final DriverManager driverManager;

    /**
     * Constructs a WebDriverConfig instance with the required DriverManager dependency.
     *
     * @param driverManager The DriverManager instance responsible for managing the WebDriver.
     */
    public WebDriverConfig(DriverManager driverManager) {
        this.driverManager = driverManager;
    }

    /**
     * Provides the existing WebDriver instance from DriverManager.
     *
     * <p>The bean is defined with a "prototype" scope to allow multiple injections,
     * but the DriverManager ensures the same WebDriver instance is returned.</p>
     *
     * @return The WebDriver instance managed by DriverManager.
     */
    @Bean
    @Lazy
    @Scope("prototype")
    public WebDriver webDriver() {
        return driverManager.getDriver();
    }
}