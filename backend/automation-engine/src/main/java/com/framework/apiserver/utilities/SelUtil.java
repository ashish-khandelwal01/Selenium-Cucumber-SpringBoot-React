package com.framework.apiserver.utilities;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Properties;

/**
 * SelUtil is a utility class for common Selenium WebDriver operations.
 * It provides methods for waiting for elements, interacting with elements, and handling visibility and clickability.
 * Extends SeleniumTestBase to utilize WebDriver setup and configuration.
 *
 * @see WebDriver
 * @see WebElement
 * @see WebDriverWait
 * @see FluentWait
 * @see Properties
 */

public class SelUtil extends SeleniumTestBase {

    /**
     * Waits for a WebElement to be visible within the specified timeout.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return The visible WebElement.
     */
    public static WebElement waitForElementToBeVisible(WebElement element, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits for a WebElement to be clickable within the specified timeout.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return The clickable WebElement.
     */
    public static WebElement waitForElementToBeClickable(WebElement element, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits for a WebElement to be selected within the specified timeout.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return True if the WebElement is selected, false otherwise.
     */
    public static boolean waitForElementToBeSelected(WebElement element, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.elementToBeSelected(element));
    }

    /**
     * Sends text to a WebElement after waiting for it to be visible.
     *
     * @param element The WebElement to send text to.
     * @param text The text to send.
     */
    public static void sendText(WebElement element, String text) {
        waitForElementToBeVisible(element, 10).sendKeys(text);
    }

    /**
     * Clicks on a WebElement after waiting for it to be clickable.
     *
     * @param element The WebElement to click.
     */
    public static void clickElement(WebElement element) {
        waitForElementToBeClickable(element, 10).click();
    }

    /**
     * Scrolls the element into view and clicks it after waiting for it to be clickable.
     *
     * @param element The WebElement to click.
     */
    public static void scrollIntoViewAndClick(WebElement element) {
        ((org.openqa.selenium.JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
        waitForElementToBeClickable(element, 10).click();
    }

    public static boolean isDisplayed(WebElement element) {
        return waitForElementToBeVisible(element, 10).isDisplayed();
    }

    /**
     * Clicks on a WebElement after waiting for it to be clickable.
     *
     * @param element The WebElement to click.
     */
    public static void clickElementFluentWait(WebElement element) {
        fluentWaitElementToBeClickable(element, 10, org.openqa.selenium.ElementClickInterceptedException.class).click();
    }

    /**
     * Clears the text of a WebElement after waiting for it to be visible.
     *
     * @param element The WebElement to clear text from.
     */
    public static void clearText(WebElement element) {
        waitForElementToBeVisible(element, 10).clear();
    }

    /**
     * Retrieves the text of a WebElement after waiting for it to be visible.
     *
     * @param element The WebElement to retrieve text from.
     * @return The text of the WebElement.
     */
    public static String getText(WebElement element) {
        return waitForElementToBeVisible(element, 10).getText();
    }

    /**
     * Waits for a WebElement to become invisible within the specified timeout.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return True if the WebElement becomes invisible, false otherwise.
     */
    public static boolean waitForElementInvisible(WebElement element, int timeout) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeout)).until(ExpectedConditions.invisibilityOf(element));
    }

    /**
     * Waits for a WebElement to be visible using FluentWait with a specified timeout and polling interval.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return The visible WebElement.
     */
    public static WebElement fluentWaitVisibilityOfElementLocated(WebElement element, int timeout) {
        return new FluentWait<>(driver).withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(5))
                .ignoring(java.util.NoSuchElementException.class)
                .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits for a WebElement to be clickable using FluentWait with a specified timeout and polling interval.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return The clickable WebElement.
     */
    public static WebElement fluentWaitElementToBeClickable(WebElement element, int timeout) {
        return new FluentWait<>(driver).withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(5))
                .ignoring(java.util.NoSuchElementException.class)
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits for a WebElement to be clickable using FluentWait with a specified timeout and polling interval.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return The clickable WebElement.
     */
    public static WebElement fluentWaitElementToBeClickable(WebElement element, int timeout, Class<? extends Throwable> exceptionToIgnore) {
        return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(exceptionToIgnore)
                .until(ExpectedConditions.elementToBeClickable(element));
    }
}