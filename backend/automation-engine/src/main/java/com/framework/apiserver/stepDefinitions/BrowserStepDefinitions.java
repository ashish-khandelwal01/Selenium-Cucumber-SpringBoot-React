package com.framework.apiserver.stepDefinitions;

import com.framework.apiserver.utilities.BaseClass;
import com.framework.apiserver.utilities.DriverManager;
import io.cucumber.java.en.Given;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;

public class BrowserStepDefinitions {

    private final DriverManager driverManager;
    @Getter
    private WebDriver driver;
    private final BaseClass baseClass;

    @Autowired
    public BrowserStepDefinitions(DriverManager driverManager
    , BaseClass baseClass) {
        this.baseClass = baseClass;
        this.driverManager = driverManager;
    }

    @Given("User wants to create a new browser instance")
    public void createBrowser() {
        driver = driverManager.createNewDriver();
        baseClass.infoLog("Created new browser instance");
    }

}
