package com.framework.apiserver.utilities;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * APIUtil provides utility methods for making HTTP requests using RestAssured.
 * This is a Spring-managed bean and should be injected where needed.
 *
 * <p>It includes methods for sending GET, POST, and DELETE requests with or without headers and body.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * {@code
 * @Autowired
 * private APIUtil apiUtil;
 *
 * Response response = apiUtil.getResponse("https://example.com");
 * }
 * </pre>
 *
 * <p>Author: ashish-khandelwal01</p>
 */
@Component
public class APIUtil{

    @Autowired
    private BaseClass baseClass;

    /**
     * Sends a GET request to the specified URL.
     *
     * @param url The URL to send the GET request to.
     * @return The response from the GET request.
     */
    public Response getResponse(String url) {
        RestAssured.baseURI = url;
        return RestAssured.given()
                .header("Content-Type", "application/json")
                .when()
                .get(url);
    }

    /**
     * Sends a POST request to the specified URL with the given body.
     *
     * @param url The URL to send the POST request to.
     * @param body The body of the POST request.
     * @return The response from the POST request.
     */
    public Response postResponse(String url, String body) {
        RestAssured.baseURI = url;
        return RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .when()
                .post(url);
    }

    /**
     * Sends a GET request to the specified URL with the given header.
     *
     * @param url The URL to send the GET request to.
     * @param header The header to include in the GET request.
     * @return The response from the GET request.
     */
    public Response getResponse(String url, Header header) {
        RestAssured.baseURI = url;
        return RestAssured.given()
                .header("Content-Type", "application/json")
                .header(header)
                .when()
                .get(url);
    }

    /**
     * Sends a POST request to the specified URL with the given body and header.
     *
     * @param url The URL to send the POST request to.
     * @param body The body of the POST request.
     * @param header The header to include in the POST request.
     * @return The response from the POST request.
     */
    public Response postResponse(String url, String body, Header header) {
        RestAssured.baseURI = url;
        return RestAssured.given()
                .header(header)
                .body(body)
                .when()
                .post(url);
    }

    /**
     * Sends a DELETE request to the specified URL.
     *
     * @param url The URL to send the DELETE request to.
     * @return The response from the DELETE request.
     */
    public Response deleteResponse(String url) {
        RestAssured.baseURI = url;
        System.out.println("URL: " + url);
        return RestAssured.given()
                .header("Content-Type", "application/json")
                .when()
                .delete(url);
    }
}