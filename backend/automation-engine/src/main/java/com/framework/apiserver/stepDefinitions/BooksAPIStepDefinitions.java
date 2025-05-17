package com.framework.apiserver.stepDefinitions;

import io.restassured.response.Response;
import io.cucumber.java.en.*;
import com.framework.apiserver.utilities.APIUtil;
import com.framework.apiserver.utilities.BaseClass;
import org.springframework.beans.factory.annotation.Autowired;
import com.framework.apiserver.service.TestExecutionService;
import org.springframework.stereotype.Component;

/**
 * BooksAPIStepDefinitions provides step definitions for interacting with the Books API.
 *
 * <p>This class contains the implementation of Cucumber steps for sending API requests
 * and validating responses. It uses RestAssured for API interactions and integrates
 * with the project's utility and service classes.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Autowired: Injects Spring-managed dependencies.</li>
 *   <li>@When, @Then, @Given: Cucumber annotations for defining step definitions.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Define Cucumber scenarios in feature files and map them to these step definitions.</li>
 *   <li>Use the provided methods to send API requests and validate responses.</li>
 * </ul>
 *
 * @see APIUtil
 * @see BaseClass
 * @see TestExecutionService
 */
public class BooksAPIStepDefinitions {

    @Autowired
    private BaseClass baseClass;

    @Autowired
    private TestExecutionService testService;

    @Autowired
    private APIUtil apiUtil;

    /**
     * Stores the response received from the API.
     */
    Response response;

    /**
     * Sends a GET request to the specified API endpoint.
     *
     * @param apiEndpoint The API endpoint to send the GET request to.
     */
    @When("User send a GET request to the API endpoint {string}")
    public void User_send_a_get_request_to_the_api_endpoint(String apiEndpoint) {
        response = apiUtil.getResponse(apiEndpoint);
    }

    /**
     * Validates that the response status code matches the expected status code.
     *
     * @param statusCode The expected status code.
     */
    @Then("User should receive a response with status code {int}")
    public void User_should_receive_a_response_with_status_code(Integer statusCode) {
        if (response.getStatusCode() == statusCode) {
            baseClass.passLog("Response status code is as expected: " + statusCode);
            baseClass.infoLog(response.asPrettyString());
        } else {
            baseClass.failLog("Expected status code: " + statusCode + ", but got: " + response.getStatusCode());
        }
    }

    /**
     * Sends a GET request to the API endpoint with the specified URL and ISBN.
     *
     * @param url  The base URL of the API endpoint.
     * @param isbn The ISBN to append to the URL.
     */
    @Given("User send a GET request to the API endpoint {string}{string}")
    public void User_send_a_get_request_to_the_api_endpoint(String url, String isbn) {
        response = apiUtil.getResponse(url + isbn);
    }
}