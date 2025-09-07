package com.framework.apiserver.stepDefinitions;

import com.framework.apiserver.pages.GooglePage;
import com.framework.apiserver.service.TestExecutionService;
import com.framework.apiserver.utilities.BaseClass;
import com.framework.apiserver.utilities.DriverManager;
import com.framework.apiserver.utilities.SelUtil;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;

public class GoogleStepDefinitions {

    private final DriverManager driverManager;
    private final BaseClass baseClass;
    private final TestExecutionService testService;
    private final SelUtil selUtil;

    private WebDriver driver;
    private GooglePage googlePage;

    /**
     * Constructs a BookStoreDemoStepDefinitions instance with the required dependencies.
     *
     * @param driverManager The DriverManager instance for managing WebDriver.
     * @param baseClass The BaseClass instance for logging and utility methods.
     * @param testService The TestExecutionService instance for test execution.
     * @param selUtil The SelUtil instance for Selenium utility methods.
     */
    @Autowired
    public GoogleStepDefinitions(DriverManager driverManager,
                                        BaseClass baseClass,
                                        TestExecutionService testService,
                                        SelUtil selUtil) {
        this.driverManager = driverManager;
        this.baseClass = baseClass;
        this.testService = testService;
        this.selUtil = selUtil;
    }

    /**
     * Initializes the page objects with the current WebDriver instance.
     */
    private void initDriverAndPages() {
        googlePage = new GooglePage(driverManager.getDriver(), selUtil);
    }

    /**
     * Ensures the WebDriver and page objects are initialized if the driver is null.
     */
    private void ensureDriverInitialized() {
        if (driver == null) {
            driver = driverManager.getDriver();
            initDriverAndPages();
        }
    }
    @Given("User navigate to google with url {string}")
    public void user_navigate_to_google_with_url(String url) {
        ensureDriverInitialized();
        googlePage.navigateToGoogle(url);

    }
    @Given("User search for {string} in google search box")
    public void user_search_for_in_google_search_box(String searchString) {
        ensureDriverInitialized();
        googlePage.searchInGoogle(searchString);
    }
    @Then("User should see the search results page")
    public void user_should_see_the_search_results_page() {
        ensureDriverInitialized();
        Assert.assertTrue(googlePage.verifySearchResultsPage());
    }
}
