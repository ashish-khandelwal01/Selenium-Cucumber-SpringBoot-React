package com.framework.apiserver.controller;

import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.service.JobTrackingService;
import com.framework.apiserver.service.TestRerunService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TestRerunController handles API endpoints for rerunning test executions.
 *
 * <p>It provides endpoints to rerun all tests or only the failed tests
 * for a specific test run identified by a run ID.</p>
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>TestRerunService: Service layer for handling test rerun logic.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/tests/rerun")
@CrossOrigin(origins = "*")
public class TestRerunController {

    @Autowired
    private TestRerunService testRerunService;

    @Autowired
    private JobTrackingService jobTrackingService;

    /**
     * Reruns all tests for the specified run ID.
     *
     * @param runId The unique identifier of the test run to rerun.
     * @return A ResponseEntity containing a TestExecutionResponse object with the rerun results.
     */
    @Operation(
            summary = "Rerun all tests for a specific run ID",
            description = "Triggers the rerun of all tests associated with the given run ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rerun completed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid run ID provided"),
                    @ApiResponse(responseCode = "500", description = "Error during rerun process")
            }
    )
    @PostMapping
    public ResponseEntity<TestExecutionResponse> rerunAll(@RequestParam String runId,
                                                          @RequestParam(defaultValue = "system") String createdBy) {
        String jobId = jobTrackingService.startSyncJob("rerun", createdBy);
        return ResponseEntity.ok(testRerunService.rerunAll(runId, jobId));
    }

    /**
     * Reruns only the failed tests for the specified run ID.
     *
     * @param runId The unique identifier of the test run to rerun failed tests.
     * @return A ResponseEntity containing a TestExecutionResponse object with the rerun results.
     */
    @Operation(
            summary = "Rerun failed tests for a specific run ID",
            description = "Triggers the rerun of only the failed tests associated with the given run ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Rerun of failed tests completed successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid run ID provided"),
                    @ApiResponse(responseCode = "500", description = "Error during rerun process")
            }
    )
    @PostMapping("/failed")
    public ResponseEntity<TestExecutionResponse> rerunFailed(@RequestParam String runId,
                                                             @RequestParam(defaultValue = "system") String createdBy) {
        return ResponseEntity.ok(testRerunService.rerunFailed(runId, createdBy));
    }
}