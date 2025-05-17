package com.framework.apiserver.stepDefinitions;

import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import com.framework.apiserver.pages.BookStorePage;
import com.framework.apiserver.pages.LoginPage;
import com.framework.apiserver.utilities.BaseClass;
import com.framework.apiserver.utilities.SeleniumTestBase;
import org.springframework.beans.factory.annotation.Autowired;
import com.framework.apiserver.service.TestExecutionService;

/**
 * Step definitions for the Book Store Demo application.
 * This class contains the implementation of Cucumber steps for interacting with the Book Store application.
 */
public class BookStoreDemoStepDefinitions extends BaseClass {

    @Autowired
    private TestExecutionService testService;

    SeleniumTestBase seleniumTestBase = new SeleniumTestBase();
    static WebDriver driver;
    LoginPage loginPage;
    BookStorePage bookStorePage;

    /**
     * Navigates the user to the Book Store application login page using the provided URL.
     * @param string The URL of the Book Store application.
     */
    @Given("User navigate to the book store application login with url {string}")
    public void User_navigate_to_the_book_store_application_login_with_url(String string) {
        driver = seleniumTestBase.browserSetup();
        loginPage = new LoginPage(driver);
        bookStorePage = new BookStorePage(driver);
        loginPage.NavigateToBookStoreApplication(string);
    }

    /**
     * Logs the user into the application using the provided username and password.
     * @param username The username to log in with.
     * @param password The password to log in with.
     */
    @Given("User log in to the application with username {string} and password {string}")
    public void User_log_in_to_the_application_with_username_and_password(String username, String password) {
        loginPage.EnterUsername(username);
        loginPage.EnterPassword(password);
        loginPage.ClickLoginButton();
    }

    /**
     * Verifies that an error message is displayed on the login page.
     */
    @Then("User should see an error message")
    public void User_should_see_an_error_message() {
        String error_message = loginPage.GetErrorMessage();
        if (error_message.equals("Invalid username or password!")) {
            passLog("Correct Error message is displayed");
        } else {
            failLog("Error message is not displayed");
        }
    }

    /**
     * Verifies that the search book field is displayed on the Book Store page.
     */
    @Then("User should see a search book field")
    public void User_should_see_a_search_book_field() {
        if (bookStorePage.isSearchBoxDisplayed()) {
            passLog("Search box is displayed");
        } else {
            failLog("Search box is not displayed");
        }
    }

    /**
     * Logs the user out of the application.
     */
    @Then("User should logout from the application")
    public void User_should_logout_from_the_application() {
        bookStorePage.clickLogoutButton();
    }

    /**
     * Verifies that a list of books is displayed on the Book Store page.
     */
    @And("User should see a list of books")
    public void User_should_see_a_list_of_books() {
        if (!bookStorePage.getBookTitles().isEmpty()) {
            passLog("List of books is displayed");
            infoLog("List of books: " + bookStorePage.getBookTitles());
        } else {
            failLog("List of books is not displayed");
        }
    }
}