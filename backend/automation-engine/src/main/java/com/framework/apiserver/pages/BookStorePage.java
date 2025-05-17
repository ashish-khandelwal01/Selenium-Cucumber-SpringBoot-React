package com.framework.apiserver.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import com.framework.apiserver.utilities.SelUtil;
import com.framework.apiserver.utilities.SeleniumTestBase;

import java.util.ArrayList;
import java.util.List;

/**
 * The BookStorePage class represents the Book Store page of the application.
 * It provides methods to interact with the page elements such as searching for books,
 * retrieving book titles, and logging out of the application.
 * This class uses the Page Object Model (POM) design pattern.
 */
public class BookStorePage extends SeleniumTestBase {
	WebDriver driver;

	/**
	 * Constructor to initialize the BookStorePage with a WebDriver instance.
	 * @param driver The WebDriver instance used to interact with the web page.
	 */
	public BookStorePage(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

	// Web elements on the Book Store page

	/** The search box element used to search for books. */
	@FindBy(id = "searchBox")
	public WebElement searchBox;

	/** A list of book title elements displayed on the page. */
	@FindBy(xpath = "//div[@class='action-buttons']/span/a")
	public List<WebElement> bookTitles;

	/** The logout button element. */
	@FindBy(xpath = "//button[@id='submit']")
	public WebElement logoutButton;

	/**
	 * Checks if the search box is displayed on the page.
	 * @return true if the search box is displayed, false otherwise.
	 */
	public boolean isSearchBoxDisplayed() {
		return SelUtil.isDisplayed(searchBox);
	}

	/**
	 * Retrieves the titles of all books displayed on the page.
	 * @return A list of book titles as Strings.
	 */
	public List<String> getBookTitles() {
		List<String> titles = new ArrayList<>();
		for (WebElement bookTitle : bookTitles) {
			titles.add(bookTitle.getText());
		}
		return titles;
	}

	/**
	 * Clicks the logout button to log out of the application.
	 */
	public void clickLogoutButton() {
		SelUtil.clickElement(logoutButton);
	}
}