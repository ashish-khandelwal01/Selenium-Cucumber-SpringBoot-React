package com.framework.apiserver.utilities;

import io.cucumber.java.Scenario;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.edge.EdgeDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

/**
 * SeleniumTestBase is a utility class for setting up and managing Selenium WebDriver instances.
 * It provides methods for browser setup, capturing screenshots, and closing the browser.
 * Extends BaseClass to utilize logging functionality.
 *
 * @see WebDriver
 * @see ChromeDriver
 * @see EdgeDriver
 * @see ChromeOptions
 * @see EdgeOptions
 * @see FileUtils
 * @see Properties
 */
public class SeleniumTestBase extends BaseClass {

    /**
     * Properties object to load configuration from a properties file.
     */
    public Properties configProperties = new Properties();

    /**
     * FileInputStream to read the configuration properties file.
     */
    public FileInputStream configFile;

    /**
     * Static block to load the configuration properties file.
     */
    {
        try {
            configFile = new FileInputStream(Constants.configFilePath);
            configProperties.load(configFile);
        } catch (Exception e) {
            failLog("Unable to load sql.properties file");
        }
    }

    /**
     * WebDriver instance for browser automation.
     */
    public static WebDriver driver;

    /**
     * ChromeOptions instance to configure Chrome browser settings.
     */
    ChromeOptions chromeOptions = new ChromeOptions();

    /**
     * EdgeOptions instance to configure Edge browser settings.
     */
    EdgeOptions edgeOptions = new EdgeOptions();

    /**
     * HashMap to store browser preferences.
     */
    HashMap<String, Object> browser_pref = new HashMap<>();

    /**
     * Sets up the browser based on the configuration properties.
     *
     * @return The WebDriver instance for the configured browser.
     */
    public WebDriver browserSetup() {
        String browser = configProperties.getProperty("browser");
        String browser_mode = configProperties.getProperty("browser_mode");
        browser_pref.put("download.default_directory", Constants.downloadPath);
        browser_pref.put("profile.default_content_setting_value.notifications", 2);
        browser_pref.put("credentials_enable_service", false);
        browser_pref.put("profile.password_manager_enabled", false);
        browser_pref.put("credential_enable_service", false);
        switch (browser) {
            case "chrome":
                chromeOptions.addArguments("user-data-dir=C:/temp/freshprofile");
                chromeOptions.addArguments("--disable-features=PasswordCheck,AutofillKeyedData,SafeBrowsingEnhancedProtection");
                chromeOptions.addArguments("--disable-sync");
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments("--disable-infobars");
                chromeOptions.addArguments("--disable-extensions");
                chromeOptions.addArguments("--disable-popup-blocking");
                chromeOptions.setExperimentalOption("prefs", browser_pref);
                if (browser_mode.equalsIgnoreCase("headless")) {
                    chromeOptions.addArguments("--headless");
                }
                driver = new ChromeDriver(chromeOptions);
                break;
            case "edge":
                edgeOptions.addArguments("--start-maximized");
                edgeOptions.addArguments("--disable-infobars");
                edgeOptions.addArguments("--disable-extensions");
                edgeOptions.addArguments("--disable-popup-blocking");
                edgeOptions.setExperimentalOption("prefs", browser_pref);
                if (browser_mode.equalsIgnoreCase("headless")) {
                    edgeOptions.addArguments("--headless");
                }
                driver = new EdgeDriver(edgeOptions);
                break;
            default:
                failLog("Browser not supported: " + browser);
        }
        return driver;
    }

    /**
     * Captures a screenshot and attaches it to the Cucumber scenario.
     *
     * @param scenario The Cucumber scenario to attach the screenshot to.
     */
    public void captureScreenshot(Scenario scenario) {
        if (driver != null) {
            final byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", scenario.getName());
        } else {
            failLog("Driver is null, unable to capture screenshot");
        }
    }

    /**
     * Closes the browser and quits the WebDriver instance.
     */
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        } else {
            failLog("Driver is null, unable to close browser");
        }
    }

    /**
     * Captures a screenshot and saves it to the specified path.
     *
     * @param screenshotName The name of the screenshot file.
     * @return The full path of the saved screenshot file.
     * @throws IOException If an error occurs while saving the screenshot.
     */
    public String captureScreenshot(String screenshotName) throws IOException {
        if (driver != null) {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            screenshotName = Constants.screenshotPath + FilenameUtils.getBaseName(screenshotName) + "_" + System.currentTimeMillis() + ".png";
            FileUtils.copyFile(screenshot, new File(screenshotName));
            return screenshotName;
        } else {
            failLog("Driver is null, unable to capture screenshot");
        }
        return null;
    }
}