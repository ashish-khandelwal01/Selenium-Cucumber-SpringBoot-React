package com.framework.apiserver.pages;

import com.framework.apiserver.utilities.SelUtil;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 * LoginPage represents the page object model for the Login page of the application.
 *
 * <p>It provides methods to interact with the elements on the Login page, such as
 * entering credentials, navigating to the page, and retrieving error messages.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@FindBy: Used to locate web elements on the page.</li>
 *   <li>@PostConstruct: Indicates that the initElements method should be executed
 *       after the bean's initialization.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Instantiate this class by passing a WebDriver and SelUtil instance.</li>
 *   <li>Use the provided methods to interact with the Login page.</li>
 * </ul>
 *
 * @see SelUtil
 */
public class LoginPage {

	private final WebDriver driver;
	private final SelUtil selUtil;

	/**
	 * WebElement representing the Book Store Application section on the page.
	 */
	@FindBy(xpath = "//h5[normalize-space()='Book Store Application']/parent::div/parent::div/parent::div")
	private WebElement bookStoreApplication;

	/**
	 * WebElement representing the Login button on the main page.
	 */
	@FindBy(xpath = "//span[normalize-space()='Login']")
	private WebElement loginPageBtn;

	/**
	 * WebElement representing the username input field.
	 */
	@FindBy(id = "userName")
	private WebElement usernameInput;

	/**
	 * WebElement representing the password input field.
	 */
	@FindBy(id = "password")
	private WebElement passwordInput;

	/**
	 * WebElement representing the Login button on the Login page.
	 */
	@FindBy(id = "login")
	private WebElement loginButton;

	/**
	 * WebElement representing the error message displayed on login failure.
	 */
	@FindBy(id = "name")
	private WebElement errorMessage;

	/**
	 * Constructs a LoginPage instance and initializes its elements.
	 *
	 * @param driver  The WebDriver instance used to interact with the browser.
	 * @param selUtil The SelUtil instance providing utility methods for Selenium.
	 */
	public LoginPage(WebDriver driver, SelUtil selUtil) {
		this.driver = driver;
		this.selUtil = selUtil;
		PageFactory.initElements(driver, this);
	}

	/**
	 * Initializes the web elements after the bean's initialization.
	 */
	@PostConstruct
	public void initElements() {
		PageFactory.initElements(driver, this);
	}

	/**
	 * Navigates to the Book Store Application and clicks the Login button.
	 *
	 * @param url The URL of the Book Store Application.
	 */
	public void navigateToBookStoreApplication(String url) {
		driver.get(url);
		selUtil.clickElement(bookStoreApplication);
		selUtil.clickElement(loginButton);
	}

	/**
	 * Enters the provided username into the username input field.
	 *
	 * @param username The username to be entered.
	 */
	public void enterUsername(String username) {
		selUtil.sendText(usernameInput, username);
	}

	/**
	 * Enters the provided password into the password input field.
	 *
	 * @param password The password to be entered.
	 */
	public void enterPassword(String password) {
		selUtil.sendText(passwordInput, password);
	}

	/**
	 * Clicks the Login button on the Login page.
	 */
	public void clickLoginButton() {
		selUtil.scrollIntoViewAndClick(loginButton);
	}

	/**
	 * Retrieves the error message displayed on login failure.
	 *
	 * @return The error message as a string.
	 */
	public String getErrorMessage() {
		return selUtil.getText(errorMessage);
	}
}