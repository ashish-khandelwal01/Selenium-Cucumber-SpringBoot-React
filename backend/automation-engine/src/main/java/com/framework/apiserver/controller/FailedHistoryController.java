package com.framework.apiserver.controller;

import com.framework.apiserver.dto.GroupRunFailures;
import com.framework.apiserver.service.TestRunFailureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST controller for handling requests related to failed test run history.
 */
@RestController
@RequestMapping("/api/history")
public class FailedHistoryController {

    @Autowired
    private TestRunFailureService testRunFailureService;

    /**
     * Retrieves all failed test runs grouped by their run IDs.
     *
     * @return A map where the key is the run ID and the value is a list of scenarios associated with that run ID.
     */
    @Operation(
            summary = "Get all failed test runs grouped by run ID",
            description = "Retrieves all failed test runs grouped by their run IDs.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Grouped failed test runs retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/failed-runs")
    public Map<String, List<String>> getAllRunsPages() {
        return testRunFailureService.getGroupedFailures();
    }

    /**
     * Retrieves a paginated list of grouped failed test runs.
     *
     * @param pageable The pagination information.
     * @return A page of GroupRunFailures objects, each containing a run ID and its associated scenarios.
     */
    @Operation(
            summary = "Get paginated grouped failed test runs",
            description = "Retrieves a paginated list of grouped failed test runs.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated grouped failed test runs retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/failed-runs/pages")
    public Page<GroupRunFailures> getPaginatedFailures(Pageable pageable) {
        return testRunFailureService.getPaginatedFailures(pageable);
    }
}