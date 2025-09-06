package com.framework.apiserver.stepDefinitions;

import com.framework.apiserver.utilities.BaseClass;
import com.framework.apiserver.utilities.DriverManager;
import io.cucumber.java.en.Given;
import org.springframework.beans.factory.annotation.Autowired;

public class BrowserStepDefinitions {

    private final DriverManager driverManager;
    private final BaseClass baseClass;

    @Autowired
    public BrowserStepDefinitions(DriverManager driverManager, BaseClass baseClass) {
        this.baseClass = baseClass;
        this.driverManager = driverManager;
    }

    @Given("User wants to create a new browser instance")
    public void createBrowser() {
        driverManager.getDriver();
    }
}