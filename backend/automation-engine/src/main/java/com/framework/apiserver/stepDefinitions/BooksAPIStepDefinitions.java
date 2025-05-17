package com.framework.apiserver.stepDefinitions;

import io.restassured.response.Response;
import io.cucumber.java.en.*;
import com.framework.apiserver.utilities.APIUtil;
import com.framework.apiserver.utilities.BaseClass;
import org.springframework.beans.factory.annotation.Autowired;
import com.framework.apiserver.service.TestExecutionService;

/**
 * Step definitions for interacting with the Books API.
 * This class contains the implementation of Cucumber steps for sending API requests
 * and validating responses.
 */
public class BooksAPIStepDefinitions extends BaseClass {

    @Autowired
    private TestExecutionService testService;

    Response response;

    /**
     * Sends a GET request to the specified API endpoint.
     * @param apiEndpoint The API endpoint to send the GET request to.
     */
    @When("User send a GET request to the API endpoint {string}")
    public void User_send_a_get_request_to_the_api_endpoint(String apiEndpoint) {
        response = APIUtil.getResponse(apiEndpoint);
    }

    /**
     * Validates that the response status code matches the expected status code.
     * @param statusCode The expected status code.
     */
    @Then("User should receive a response with status code {int}")
    public void User_should_receive_a_response_with_status_code(Integer statusCode) {
        if(response.getStatusCode() == statusCode) {
            passLog("Response status code is as expected: " + statusCode);
            infoLog(response.asPrettyString());
        } else {
            failLog("Expected status code: " + statusCode + ", but got: " + response.getStatusCode());
        }
    }

    /**
     * Sends a GET request to the API endpoint with the specified URL and ISBN.
     * @param url The base URL of the API endpoint.
     * @param isbn The ISBN to append to the URL.
     */
    @Given("User send a GET request to the API endpoint {string}{string}")
    public void User_send_a_get_request_to_the_api_endpoint(String url, String isbn) {
        response = APIUtil.getResponse(url + isbn);
    }


}