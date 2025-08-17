package com.framework.apiserver.controller;

import com.framework.apiserver.config.JobStatus;
import com.framework.apiserver.service.TestExecutionService;
import com.framework.apiserver.service.TestRerunService;
import com.framework.apiserver.utilities.AsyncJobManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * REST controller for managing asynchronous test executions and reruns.
 * Provides endpoints for initiating, monitoring, and canceling asynchronous jobs.
 */
@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "*")
public class AsyncTestController {

    @Autowired
    private TestExecutionService testExecutionService;

    @Autowired
    private TestRerunService testRerunService;

    @Autowired
    private AsyncJobManager asyncJobManager;

    /**
     * Initiates an asynchronous test execution based on the provided tags.
     *
     * @param tags The tags to filter the tests to be executed.
     * @return A ResponseEntity containing a map with the generated job ID.
     */
    @Operation(
            summary = "Run tests asynchronously",
            description = "Initiates an asynchronous test execution based on the provided tags.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job initiated successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/async-run")
    public ResponseEntity<Map<String, String>> runAsync(@RequestParam String tags,
                                                        @RequestParam(defaultValue = "system") String createdBy) {
        String jobId = testExecutionService.runTestsAsync(tags, createdBy);
        return ResponseEntity.ok(Collections.singletonMap("jobId", jobId));
    }

    /**
     * Initiates an asynchronous rerun of tests for a specific run ID.
     *
     * @param runId The unique identifier of the test run to be rerun.
     * @return A ResponseEntity containing a map with the generated job ID.
     */
    @Operation(
            summary = "Rerun tests asynchronously",
            description = "Initiates an asynchronous rerun of tests for a specific run ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job initiated successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/async-rerun")
    public ResponseEntity<Map<String, String>> rerunAsync(@RequestParam String runId,
                                                          @RequestParam(defaultValue = "system") String createdBy) {
        String jobId = testRerunService.rerunTestsAsync(runId, createdBy);
        return ResponseEntity.ok(Collections.singletonMap("jobId", jobId));
    }

    /**
     * Initiates an asynchronous rerun of only the failed tests for a specific run ID.
     *
     * @param runId The unique identifier of the test run to rerun failed tests.
     * @return A ResponseEntity containing a map with the generated job ID.
     */
    @Operation(
            summary = "Rerun failed tests asynchronously",
            description = "Initiates an asynchronous rerun of only the failed tests for a specific run ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job initiated successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/async-rerun/failed")
    public ResponseEntity<Map<String, String>> rerunFailedAsync(@RequestParam String runId,
                                                                @RequestParam(defaultValue = "system") String createdBy) {
        String jobId = testRerunService.rerunFailedTestsAsync(runId, createdBy);
        return ResponseEntity.ok(Collections.singletonMap("jobId", jobId));
    }

    /**
     * Retrieves the status of a specific asynchronous job.
     *
     * @param jobId The unique identifier of the job.
     * @return A ResponseEntity containing the job status and additional details if available.
     */
    @Operation(
            summary = "Get job status",
            description = "Retrieves the status of a specific asynchronous job.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job status retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Job not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/status/{jobId}")
    public ResponseEntity<?> getJobStatus(@PathVariable String jobId) {
        JobStatus status = asyncJobManager.getStatus(jobId);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Invalid jobId: " + jobId);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("jobId", jobId);
        response.put("status", status);

        if (status == JobStatus.COMPLETED) {
            response.put("result", asyncJobManager.getResult(jobId));
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Attempts to cancel an ongoing asynchronous job using jobId.
     *
     * @param jobId The unique identifier of the job to be canceled.
     * @return A ResponseEntity indicating whether the job was successfully canceled or not.
     */
    @Operation(
            summary = "Cancel an async job",
            description = "Attempts to cancel an ongoing asynchronous job using jobId.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job cancelled successfully"),
                    @ApiResponse(responseCode = "404", description = "Job not found or already completed")
            }
    )
    @DeleteMapping("/cancel/{jobId}")
    public ResponseEntity<?> cancelJob(@PathVariable String jobId) {
        boolean cancelled = asyncJobManager.cancelJob(jobId);
        if (cancelled) {
            return ResponseEntity.ok("Job cancelled successfully");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Job not found or already completed");
        }
    }

}