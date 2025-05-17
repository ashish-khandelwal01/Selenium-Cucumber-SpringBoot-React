package com.framework.apiserver.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.framework.apiserver.utilities.SelUtil;
import com.framework.apiserver.utilities.SeleniumTestBase;

/**
 * The LoginPage class represents the login page of the Book Store Application.
 * It provides methods to interact with the login page elements and perform actions like entering credentials and logging in.
 * This class uses the Page Object Model (POM) design pattern.
 */
public class LoginPage extends SeleniumTestBase {
	WebDriver driver;

	/**
	 * Constructor to initialize the LoginPage with a WebDriver instance.
	 * @param driver The WebDriver instance used to interact with the web page.
	 */
	public LoginPage(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

	// Web elements on the login page

	/** The Book Store Application section on the page. */
	@FindBy(xpath = "//h5[normalize-space()='Book Store Application']/parent::div/parent::div/parent::div")
	public WebElement bookStoreApplication;

	/** The button to navigate to the login page. */
	@FindBy(xpath = "//span[normalize-space()='Login']")
	public WebElement login_page_btn;

	/** The input field for the username. */
	@FindBy(id = "userName")
	public WebElement username_input;

	/** The input field for the password. */
	@FindBy(id = "password")
	public WebElement password_input;

	/** The login button on the page. */
	@FindBy(id = "login")
	public WebElement login_button;

	/** The error message displayed on login failure. */
	@FindBy(id = "name")
	public WebElement error_message;

	/**
	 * Navigates to the Book Store Application and clicks the login button.
	 * @param url The URL of the Book Store Application.
	 */
	public void NavigateToBookStoreApplication(String url) {
		driver.get(url);
		SelUtil.clickElement(bookStoreApplication);
		SelUtil.clickElement(login_button);
	}

	/**
	 * Enters the username into the username input field.
	 * @param username The username to be entered.
	 */
	public void EnterUsername(String username) {
		SelUtil.sendText(username_input, username);
	}

	/**
	 * Enters the password into the password input field.
	 * @param password The password to be entered.
	 */
	public void EnterPassword(String password) {
		SelUtil.sendText(password_input, password);
	}

	/**
	 * Clicks the login button using a fluent wait.
	 */
	public void ClickLoginButton() {
		SelUtil.scrollIntoViewAndClick(login_button);
	}

	/**
	 * Retrieves the error message displayed on the page.
	 * @return The error message as a String.
	 */
	public String GetErrorMessage() {
		return SelUtil.getText(error_message);
	}
}