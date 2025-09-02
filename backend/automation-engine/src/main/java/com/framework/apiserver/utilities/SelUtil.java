package com.framework.apiserver.utilities;

import lombok.Getter;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * SelUtil is a utility class for common Selenium WebDriver operations.
 * It provides methods for waiting, interacting with elements, and performing actions on web pages.
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>Spring Framework for dependency injection</li>
 *   <li>Selenium WebDriver for browser automation</li>
 * </ul>
 *
 * @see WebDriver
 * @see WebElement
 * @see WebDriverWait
 * @see FluentWait
 * @see Component
 */
@Getter
@Component
public class SelUtil {

    @Autowired
    @Lazy
    protected WebDriver driver;
    private final SeleniumTestBase seleniumTestBase;

    /**
     * Constructs a SelUtil instance with the required SeleniumTestBase dependency.
     *
     * @param seleniumTestBase The SeleniumTestBase instance for WebDriver management.
     */
    @Autowired
    public SelUtil(SeleniumTestBase seleniumTestBase) {
        this.seleniumTestBase = seleniumTestBase;
    }

    /**
     * Waits for a WebElement to be visible within the specified timeout.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return The visible WebElement.
     */
    public WebElement waitForElementToBeVisible(WebElement element, int timeout) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeout))
                .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits for a WebElement to be clickable within the specified timeout.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return The clickable WebElement.
     */
    public WebElement waitForElementToBeClickable(WebElement element, int timeout) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeout))
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits for a WebElement to be selected within the specified timeout.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return True if the element is selected, false otherwise.
     */
    public boolean waitForElementToBeSelected(WebElement element, int timeout) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeout))
                .until(ExpectedConditions.elementToBeSelected(element));
    }

    /**
     * Sends text to a WebElement after waiting for it to be visible.
     *
     * @param element The WebElement to send text to.
     * @param text    The text to send.
     */
    public void sendText(WebElement element, String text) {
        waitForElementToBeVisible(element, 10).sendKeys(text);
    }

    /**
     * Clicks a WebElement after waiting for it to be clickable.
     *
     * @param element The WebElement to click.
     */
    public void clickElement(WebElement element) {
        waitForElementToBeClickable(element, 10).click();
    }

    /**
     * Scrolls to a WebElement and clicks it.
     *
     * @param element The WebElement to scroll to and click.
     */
    public void scrollIntoViewAndClick(WebElement element) {
        WebDriver actualDriver = getActualDriver(driver);
        ((JavascriptExecutor) actualDriver).executeScript("arguments[0].scrollIntoView(true);", element);
        waitForElementToBeClickable(element, 10).click();
    }

    /**
     * Checks if a WebElement is displayed after waiting for it to be visible.
     *
     * @param element The WebElement to check.
     * @return True if the element is displayed, false otherwise.
     */
    public boolean isDisplayed(WebElement element) {
        return waitForElementToBeVisible(element, 10).isDisplayed();
    }

    /**
     * Clicks a WebElement using a FluentWait for it to be clickable.
     *
     * @param element The WebElement to click.
     */
    public void clickElementFluentWait(WebElement element) {
        fluentWaitElementToBeClickable(element, 10, ElementClickInterceptedException.class).click();
    }

    /**
     * Clears the text of a WebElement after waiting for it to be visible.
     *
     * @param element The WebElement to clear.
     */
    public void clearText(WebElement element) {
        waitForElementToBeVisible(element, 10).clear();
    }

    /**
     * Retrieves the text of a WebElement after waiting for it to be visible.
     *
     * @param element The WebElement to retrieve text from.
     * @return The text of the WebElement.
     */
    public String getText(WebElement element) {
        return waitForElementToBeVisible(element, 10).getText();
    }

    /**
     * Waits for a WebElement to become invisible within the specified timeout.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return True if the element becomes invisible, false otherwise.
     */
    public boolean waitForElementInvisible(WebElement element, int timeout) {
        return new WebDriverWait(getDriver(), Duration.ofSeconds(timeout))
                .until(ExpectedConditions.invisibilityOf(element));
    }

    /**
     * Waits for a WebElement to be visible using FluentWait.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return The visible WebElement.
     */
    public WebElement fluentWaitVisibilityOfElementLocated(WebElement element, int timeout) {
        return new FluentWait<>(getDriver())
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.visibilityOf(element));
    }

    /**
     * Waits for a WebElement to be clickable using FluentWait.
     *
     * @param element The WebElement to wait for.
     * @param timeout The timeout in seconds.
     * @return The clickable WebElement.
     */
    public WebElement fluentWaitElementToBeClickable(WebElement element, int timeout) {
        return new FluentWait<>(getDriver())
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Waits for a WebElement to be clickable using FluentWait, ignoring a specific exception.
     *
     * @param element          The WebElement to wait for.
     * @param timeout          The timeout in seconds.
     * @param exceptionToIgnore The exception to ignore during the wait.
     * @return The clickable WebElement.
     */
    public WebElement fluentWaitElementToBeClickable(WebElement element, int timeout, Class<? extends Throwable> exceptionToIgnore) {
        return new FluentWait<>(getDriver())
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(1))
                .ignoring(exceptionToIgnore)
                .until(ExpectedConditions.elementToBeClickable(element));
    }

    /**
     * Retrieves the actual WebDriver instance, unwrapping any Spring AOP proxy.
     *
     * @param driver The potentially proxied WebDriver instance.
     * @return The actual WebDriver instance.
     */
    public WebDriver getActualDriver(WebDriver driver) {
        if (AopUtils.isAopProxy(driver) && driver instanceof Advised) {
            try {
                return (WebDriver) ((Advised) driver).getTargetSource().getTarget();
            } catch (Exception e) {
                throw new RuntimeException("Failed to unwrap proxied WebDriver", e);
            }
        }
        return driver;
    }
}