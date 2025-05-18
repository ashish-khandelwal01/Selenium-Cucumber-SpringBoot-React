package com.framework.apiserver.utilities;

import com.framework.apiserver.config.FrameworkProperties;
import com.framework.apiserver.config.MySqlDbProperties;
import io.cucumber.java.Scenario;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * SeleniumTestBase provides utility methods for setting up browsers, capturing screenshots,
 * and managing WebDriver instances for Selenium-based tests.
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>Spring Framework for dependency injection</li>
 *   <li>Selenium WebDriver for browser automation</li>
 *   <li>Apache Commons IO for file operations</li>
 * </ul>
 *
 * @see WebDriver
 * @see ChromeDriver
 * @see EdgeDriver
 * @see TakesScreenshot
 * @see FileUtils
 * @see Component
 */
@Component
public class SeleniumTestBase {

    private final FrameworkProperties properties;

    @Autowired
    private BaseClass baseClass;

    @Setter
    @Getter
    protected WebDriver driver;

    @Value("${browser}")
    private String browser;

    @Value("${browser_mode}")
    private String browserMode;

    private final HashMap<String, Object> browserPref = new HashMap<>();

    /**
     * Constructs a SeleniumTestBase instance and initializes browser preferences.
     */
    public SeleniumTestBase(FrameworkProperties properties) {
        this.properties = properties;
        browserPref.put("download.default_directory", properties.getDownloadPath());
        browserPref.put("profile.default_content_setting_value.notifications", 2);
        browserPref.put("credentials_enable_service", false);
        browserPref.put("profile.password_manager_enabled", false);
        browserPref.put("credential_enable_service", false);
    }

    /**
     * Sets up the browser based on the configuration properties.
     *
     * <p>Supported browsers:</p>
     * <ul>
     *   <li>Chrome</li>
     *   <li>Edge</li>
     * </ul>
     *
     * @return The WebDriver instance.
     * @throws IllegalArgumentException if the browser is not supported.
     */
    public WebDriver browserSetup() {
        switch (browser.toLowerCase()) {
            case "chrome":
                ChromeOptions chromeOptions = new ChromeOptions();
                chromeOptions.addArguments("--disable-features=PasswordCheck,AutofillKeyedData,SafeBrowsingEnhancedProtection");
                chromeOptions.addArguments("--disable-sync");
                chromeOptions.addArguments("--start-maximized");
                chromeOptions.addArguments("--disable-infobars");
                chromeOptions.addArguments("--disable-extensions");
                chromeOptions.addArguments("--disable-popup-blocking");
                chromeOptions.setExperimentalOption("prefs", browserPref);

                if ("headless".equalsIgnoreCase(browserMode)) {
                    chromeOptions.addArguments("--headless");
                    chromeOptions.addArguments("--window-size=1600,900");
                    chromeOptions.addArguments("--no-sandbox");
                    chromeOptions.addArguments("--disable-dev-shm-usage");
                    chromeOptions.addArguments("--disable-gpu");
                }

                driver = new ChromeDriver(chromeOptions);
                break;

            case "edge":
                EdgeOptions edgeOptions = new EdgeOptions();
                edgeOptions.addArguments("--start-maximized");
                edgeOptions.addArguments("--disable-infobars");
                edgeOptions.addArguments("--disable-extensions");
                edgeOptions.addArguments("--disable-popup-blocking");
                edgeOptions.setExperimentalOption("prefs", browserPref);

                if ("headless".equalsIgnoreCase(browserMode)) {
                    edgeOptions.addArguments("--headless");
                    edgeOptions.addArguments("--no-sandbox");
                    edgeOptions.addArguments("--disable-dev-shm-usage");
                    edgeOptions.addArguments("--window-size=1600,900");
                    edgeOptions.addArguments("--disable-gpu");
                }

                driver = new EdgeDriver(edgeOptions);
                break;

            default:
                baseClass.failLog("Browser not supported: " + browser);
                throw new IllegalArgumentException("Unsupported browser: " + browser);
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
            byte[] screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            scenario.attach(screenshot, "image/png", scenario.getName());
        } else {
            baseClass.failLog("Driver is null, unable to capture screenshot");
        }
    }

    /**
     * Closes the browser and quits the WebDriver instance.
     */
    public void closeBrowser() {
        if (driver != null) {
            driver.quit();
        } else {
            baseClass.failLog("Driver is null, unable to close browser");
        }
    }

    /**
     * Captures a screenshot and saves it to the specified path.
     *
     * @param screenshotName The name of the screenshot file.
     * @return The full path of the saved screenshot file.
     * @throws IOException if saving the screenshot fails.
     */
    public String captureScreenshot(String screenshotName) throws IOException {
        if (driver != null) {
            File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String filePath = properties.getScreenshotPath()
                    + FilenameUtils.getBaseName(screenshotName)
                    + "_" + System.currentTimeMillis() + ".png";
            FileUtils.copyFile(screenshot, new File(filePath));
            return filePath;
        } else {
            baseClass.failLog("Driver is null, unable to capture screenshot");
            return null;
        }
    }
}