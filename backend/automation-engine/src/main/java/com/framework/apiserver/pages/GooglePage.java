package com.framework.apiserver.pages;

import com.framework.apiserver.utilities.SelUtil;
import jakarta.annotation.PostConstruct;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class GooglePage {

    private final WebDriver driver;
    private final SelUtil selUtil;

    /**
     * WebElement representing the search box on the Book Store page.
     */
    @FindBy(xpath = "//input[@id='searchbox_input']")
    private WebElement searchBox;

    @FindBy(xpath = "//button[@aria-label='Search']")
    private WebElement searchButton;

    /**
     * List of WebElements representing the book titles displayed on the page.
     */
    @FindBy(xpath = "//li[@data-layout='organic']")
    private List<WebElement> results;

    @FindBy(xpath = "//a[text()='All']")
    private WebElement allTab;

    /**
     * Constructs a BookStorePage instance and initializes its elements.
     *
     * @param driver  The WebDriver instance used to interact with the browser.
     * @param selUtil The SelUtil instance providing utility methods for Selenium.
     */
    public GooglePage(WebDriver driver, SelUtil selUtil) {
        this.driver = driver;
        this.selUtil = selUtil;
        PageFactory.initElements(driver, this);
    }

    @PostConstruct
    public void initElements() {
        PageFactory.initElements(driver, this);
    }

    public void navigateToGoogle(String url) {
        driver.get(url);
    }


    public void searchInGoogle(String searchString) {
        selUtil.fluentWaitVisibilityOfElementLocated(searchBox, 10);
        selUtil.sendText(searchBox, searchString);
        selUtil.fluentWaitElementToBeClickable(searchButton, 10);
        selUtil.clickElement(searchButton);
    }

    public boolean verifySearchResultsPage() {
        selUtil.fluentWaitVisibilityOfElementLocated(allTab, 10);
        return !results.isEmpty();
    }
}
