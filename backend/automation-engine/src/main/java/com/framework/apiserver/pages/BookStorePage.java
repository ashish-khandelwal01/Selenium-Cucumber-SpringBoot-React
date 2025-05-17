package com.framework.apiserver.pages;

import com.framework.apiserver.utilities.SelUtil;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * BookStorePage represents the page object model for the Book Store page.
 *
 * <p>It provides methods to interact with the elements on the page, such as
 * searching for books, retrieving book titles, and logging out.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@FindBy: Used to locate web elements on the page.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Instantiate this class by passing a WebDriver and SelUtil instance.</li>
 *   <li>Use the provided methods to interact with the Book Store page.</li>
 * </ul>
 *
 * @see SelUtil
 */
public class BookStorePage {

	private final WebDriver driver;
	private final SelUtil selUtil;

	/**
	 * WebElement representing the search box on the Book Store page.
	 */
	@FindBy(id = "searchBox")
	private WebElement searchBox;

	/**
	 * List of WebElements representing the book titles displayed on the page.
	 */
	@FindBy(xpath = "//div[@class='action-buttons']/span/a")
	private List<WebElement> bookTitles;

	/**
	 * WebElement representing the logout button on the Book Store page.
	 */
	@FindBy(xpath = "//button[@id='submit']")
	private WebElement logoutButton;

	/**
	 * Constructs a BookStorePage instance and initializes its elements.
	 *
	 * @param driver  The WebDriver instance used to interact with the browser.
	 * @param selUtil The SelUtil instance providing utility methods for Selenium.
	 */
	public BookStorePage(WebDriver driver, SelUtil selUtil) {
		this.driver = driver;
		this.selUtil = selUtil;
		PageFactory.initElements(driver, this);
	}

	/**
	 * Checks if the search box is displayed on the page.
	 *
	 * @return true if the search box is displayed, false otherwise.
	 */
	public boolean isSearchBoxDisplayed() {
		return selUtil.isDisplayed(searchBox);
	}

	/**
	 * Retrieves the titles of the books displayed on the page.
	 *
	 * @return A list of book titles as strings.
	 */
	public List<String> getBookTitles() {
		List<String> titles = new ArrayList<>();
		for (WebElement bookTitle : bookTitles) {
			titles.add(bookTitle.getText());
		}
		return titles;
	}

	/**
	 * Clicks the logout button to log out of the Book Store page.
	 */
	public void clickLogoutButton() {
		selUtil.clickElement(logoutButton);
	}
}