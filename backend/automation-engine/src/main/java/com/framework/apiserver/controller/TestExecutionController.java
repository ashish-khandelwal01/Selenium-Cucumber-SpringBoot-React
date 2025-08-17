package com.framework.apiserver.controller;

import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.service.JobTrackingService;
import com.framework.apiserver.service.TestExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * TestExecutionController is a REST controller that handles API requests
 * related to test execution.
 *
 * <p>It provides an endpoint to trigger the execution of Cucumber tests
 * based on specified tags.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@RestController: Marks this class as a Spring REST controller.</li>
 *   <li>@RequestMapping: Maps requests to "/api/tests" at the class level.</li>
 *   <li>@PostMapping: Maps POST requests to the "/run" endpoint.</li>
 *   <li>@Autowired: Injects the TestExecutionService dependency.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Send a POST request to "/api/tests/run" with an optional "tags" parameter
 *       to execute tests filtered by the specified tags.</li>
 * </ul>
 *
 * @see TestExecutionService
 * @see TestExecutionResponse
 */
@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "*")
public class TestExecutionController {

    @Autowired
    private TestExecutionService testExecutionService;

    @Autowired
    private JobTrackingService jobTrackingService;

    /**
     * Executes Cucumber tests based on the provided tags.
     *
     * @param tags A comma-separated list of tags to filter the tests (optional).
     *             Defaults to an empty string if not provided.
     * @return A TestExecutionResponse object containing the results of the test execution.
     */
    @Operation(
            summary = "Execute Cucumber tests with a specific tag",
            description = "Triggers the execution of Cucumber tests filtered by the provided tag and returns execution status.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test execution completed successfully"),
                    @ApiResponse(responseCode = "500", description = "Error during test execution")
            }
    )
    @PostMapping("/run")
    public TestExecutionResponse runTests(@RequestParam(defaultValue = "") String tags,
                                          @RequestParam(defaultValue = "system") String createdBy) {
        String jobId = jobTrackingService.startSyncJob(tags, createdBy);
        return testExecutionService.runCucumberTests(tags, jobId, false);
    }

}