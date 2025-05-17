package com.framework.apiserver.dto;

import lombok.Getter;

/**
 * TestExecutionResponse is a Data Transfer Object (DTO) that encapsulates
 * the response details of a test execution.
 *
 * <p>It provides information about the execution status and the exit code
 * of the test process.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Getter: Lombok annotation to automatically generate getter methods
 *       for all fields in the class.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Create an instance of this class to represent the result of a test execution.</li>
 *   <li>Access the status and exit code using the generated getter methods.</li>
 * </ul>
 */
@Getter
public class TestExecutionResponse {

    /**
     * The status of the test execution (e.g., "SUCCESS", "FAILURE").
     */
    private String status;

    /**
     * The exit code of the test execution process.
     */
    private int exitCode;

    /**
     * Constructs a TestExecutionResponse instance with the specified status and exit code.
     *
     * @param status The status of the test execution.
     * @param exitCode The exit code of the test execution process.
     */
    public TestExecutionResponse(String status, int exitCode) {
        this.status = status;
        this.exitCode = exitCode;
    }

}