package com.framework.apiserver.utilities;

import com.framework.apiserver.config.FrameworkProperties;
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
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
    public WebDriver browserSetup(String browser) {
        WebDriver driver = null;
        String gridUrl = System.getenv("GRID_URL");
        System.out.println("GRID URL: " + gridUrl);
        try {
            boolean useGrid = useGrid(gridUrl);
            System.out.println("Browser: " + browser + ", Mode: " + browserMode + ", Using Grid: " + useGrid);
            String b = browser.toLowerCase();

            switch (b) {
                case "chrome":
                    ChromeOptions chromeOptions = new ChromeOptions();
                    chromeOptions.addArguments("--disable-features=PasswordCheck,AutofillKeyedData,SafeBrowsingEnhancedProtection");
                    chromeOptions.addArguments("--disable-sync");
                    chromeOptions.addArguments("--start-maximized");
                    chromeOptions.addArguments("--disable-infobars");
                    chromeOptions.addArguments("--disable-extensions");
                    chromeOptions.addArguments("--disable-popup-blocking");
                    if (browserPref != null) {
                        chromeOptions.setExperimentalOption("prefs", browserPref);
                    }

                    if ("headless".equalsIgnoreCase(browserMode)) {
                        chromeOptions.addArguments("--headless");
                        chromeOptions.addArguments("--window-size=1920,1080");
                        chromeOptions.addArguments("--no-sandbox");
                        chromeOptions.addArguments("--disable-dev-shm-usage");
                        chromeOptions.addArguments("--disable-gpu");
                    }

                    if (useGrid) {
                        driver = new RemoteWebDriver(new URL(gridUrl), chromeOptions);
                        System.out.println("Running tests on Selenium Grid: " + gridUrl);
                    } else {
                        driver = new ChromeDriver(chromeOptions);
                    }
                    break;

                case "firefox":
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (browserPref != null) {
                        firefoxOptions.addPreference("browser.download.dir", "src/test/resources/downloads/");
                        firefoxOptions.addPreference("browser.download.folderList", 2);
                        firefoxOptions.addPreference("browser.download.useDownloadDir", true);
                        firefoxOptions.addPreference("browser.download.manager.showWhenStarting", false);
                        firefoxOptions.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/pdf");
                        firefoxOptions.addPreference("pdfjs.disabled", true);

                        firefoxOptions.addPreference("profile.default_content_setting_value.notifications", 2);
                        firefoxOptions.addPreference("signon.rememberSignons", false);
                    }
                    if ("headless".equalsIgnoreCase(browserMode)) {
                        firefoxOptions.addArguments("--headless");
                        firefoxOptions.addArguments("--width=1920");
                        firefoxOptions.addArguments("--height=1080");
                    }

                    if (useGrid) {
                        System.out.println("Running tests on Selenium Grid: " + gridUrl);
                        driver = new RemoteWebDriver(new URL(gridUrl), firefoxOptions);
                    } else {
                        driver = new FirefoxDriver(firefoxOptions);
                    }
                    break;

                case "edge":
                    EdgeOptions edgeOptions = new EdgeOptions();
                    edgeOptions.addArguments("--start-maximized");
                    edgeOptions.addArguments("--disable-infobars");
                    edgeOptions.addArguments("--disable-extensions");
                    edgeOptions.addArguments("--disable-popup-blocking");
                    if (browserPref != null) {
                        edgeOptions.setExperimentalOption("prefs", browserPref);
                    }

                    if ("headless".equalsIgnoreCase(browserMode)) {
                        edgeOptions.addArguments("--headless");
                        edgeOptions.addArguments("--no-sandbox");
                        edgeOptions.addArguments("--disable-dev-shm-usage");
                        edgeOptions.addArguments("--window-size=1920,1080");
                        edgeOptions.addArguments("--disable-gpu");
                    }

                    if (useGrid) {
                        System.out.println("Running tests on Selenium Grid: " + gridUrl);
                        driver = new RemoteWebDriver(new URL(gridUrl), edgeOptions);
                    } else {
                        driver = new EdgeDriver(edgeOptions);
                    }
                    break;

                default:
                    baseClass.failLog("Browser not supported: " + browser);
                    throw new IllegalArgumentException("Unsupported browser: " + browser);
            }

        } catch (Exception e) {
            baseClass.failLog("Error initializing browser: " + e.getMessage());
        }

        return driver;
    }

    /**
     * Determines whether to use Selenium Grid for running tests based on the provided Grid URL.
     *
     * <p>This method checks if the Selenium Grid is available and ready to use by sending a GET request
     * to the Grid's status endpoint. If the Grid is ready, it returns true; otherwise, it falls back to
     * using a local WebDriver instance.</p>
     *
     * @param gridUrl The URL of the Selenium Grid, including the protocol (e.g., http://localhost:4444).
     * @return {@code true} if the Selenium Grid is available and ready; {@code false} otherwise.
     */
    private boolean useGrid(String gridUrl) {
        boolean useGrid = false;

        if (gridUrl == null || gridUrl.trim().isEmpty()) {
            System.out.println("Grid URL is null or empty, using local driver");
            return false;
        }

        try {
            URL url = new URL(gridUrl + "/status");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000); // 10 seconds - increased timeout
            conn.setReadTimeout(10000); // 10 seconds read timeout
            conn.connect();

            int responseCode = conn.getResponseCode();
            System.out.println("Grid status check response code: " + responseCode);

            if (responseCode == 200) {
                // Additional check to verify grid is ready
                try {
                    // Read response to ensure grid is actually ready
                    String response = new String(conn.getInputStream().readAllBytes());
                    if (response.contains("\"ready\": true") || response.contains("ready\":true")) {
                        useGrid = true;
                        System.out.println("Selenium Grid is ready and available");
                    } else {
                        System.out.println("Selenium Grid responded but is not ready yet");
                    }
                } catch (Exception e) {
                    System.out.println("Error reading grid status response: " + e.getMessage());
                }
            } else {
                System.out.println("Grid not available, response code: " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            System.out.println("Grid connection failed: " + e.getMessage());
            useGrid = false;
        }

        System.out.println("Using Grid: " + useGrid);
        return useGrid;
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