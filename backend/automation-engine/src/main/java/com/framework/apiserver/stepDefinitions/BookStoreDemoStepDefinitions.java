package com.framework.apiserver.stepDefinitions;

import com.framework.apiserver.pages.BookStorePage;
import com.framework.apiserver.pages.LoginPage;
import com.framework.apiserver.utilities.BaseClass;
import com.framework.apiserver.utilities.DriverManager;
import com.framework.apiserver.utilities.SelUtil;
import com.framework.apiserver.service.TestExecutionService;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * BookStoreDemoStepDefinitions provides step definitions for interacting with the Book Store application.
 *
 * <p>This class contains the implementation of Cucumber steps for browser interactions,
 * navigating to the application, logging in, and verifying UI elements.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Autowired: Injects Spring-managed dependencies.</li>
 *   <li>@Given, @Then, @And: Cucumber annotations for defining step definitions.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Define Cucumber scenarios in feature files and map them to these step definitions.</li>
 *   <li>Use the provided methods to interact with the Book Store application.</li>
 * </ul>
 *
 * @see BookStorePage
 * @see LoginPage
 * @see BaseClass
 * @see DriverManager
 * @see SelUtil
 * @see TestExecutionService
 */
public class BookStoreDemoStepDefinitions {

    private final DriverManager driverManager;
    private final BaseClass baseClass;
    private final TestExecutionService testService;
    private final SelUtil selUtil;

    private WebDriver driver;
    private LoginPage loginPage;
    private BookStorePage bookStorePage;

    /**
     * Constructs a BookStoreDemoStepDefinitions instance with the required dependencies.
     *
     * @param driverManager The DriverManager instance for managing WebDriver.
     * @param baseClass The BaseClass instance for logging and utility methods.
     * @param testService The TestExecutionService instance for test execution.
     * @param selUtil The SelUtil instance for Selenium utility methods.
     */
    @Autowired
    public BookStoreDemoStepDefinitions(DriverManager driverManager,
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
        loginPage = new LoginPage(driver, selUtil);
        bookStorePage = new BookStorePage(driver, selUtil);
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

    /**
     * Creates a new browser instance and initializes the page objects.
     */
    @Given("User wants to create a new browser instance")
    public void userCreatesNewBrowser() {
        this.driver = driverManager.createNewDriver();
        initDriverAndPages();
        baseClass.infoLog("Created new browser instance");
    }

    /**
     * Navigates to the Book Store application login page using the provided URL.
     *
     * @param url The URL of the Book Store application login page.
     */
    @Given("User navigate to the book store application login with url {string}")
    public void userNavigateToBookStoreApplicationLoginWithUrl(String url) {
        ensureDriverInitialized();
        loginPage.navigateToBookStoreApplication(url);
    }

    /**
     * Logs in to the application using the provided username and password.
     *
     * @param username The username for login.
     * @param password The password for login.
     */
    @Given("User log in to the application with username {string} and password {string}")
    public void userLogInToTheApplicationWithUsernameAndPassword(String username, String password) {
        ensureDriverInitialized();
        loginPage.enterUsername(username);
        loginPage.enterPassword(password);
        loginPage.clickLoginButton();
    }

    /**
     * Verifies that an error message is displayed on the login page.
     */
    @Then("User should see an error message")
    public void userShouldSeeAnErrorMessage() {
        ensureDriverInitialized();
        String errorMessage = loginPage.getErrorMessage();
        if ("Invalid username or password!".equals(errorMessage)) {
            baseClass.passLog("Correct error message is displayed");
        } else {
            baseClass.failLog("Error message is not displayed or incorrect");
        }
    }

    /**
     * Verifies that the search book field is displayed on the Book Store page.
     */
    @Then("User should see a search book field")
    public void userShouldSeeASearchBookField() {
        ensureDriverInitialized();
        if (bookStorePage.isSearchBoxDisplayed()) {
            baseClass.passLog("Search box is displayed");
        } else {
            baseClass.failLog("Search box is not displayed");
        }
    }

    /**
     * Logs out from the Book Store application.
     */
    @Then("User should logout from the application")
    public void userShouldLogoutFromTheApplication() {
        ensureDriverInitialized();
        bookStorePage.clickLogoutButton();
    }

    /**
     * Verifies that a list of books is displayed on the Book Store page.
     */
    @And("User should see a list of books")
    public void userShouldSeeAListOfBooks() {
        ensureDriverInitialized();
        var titles = bookStorePage.getBookTitles();
        if (!titles.isEmpty()) {
            baseClass.passLog("List of books is displayed");
            baseClass.infoLog("List of books: " + titles);
        } else {
            baseClass.failLog("List of books is not displayed");
        }
    }
}