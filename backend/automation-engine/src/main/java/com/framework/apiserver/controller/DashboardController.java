package com.framework.apiserver.controller;

import com.framework.apiserver.dto.dashboard.PassFailPieResponse;
import com.framework.apiserver.dto.dashboard.TopFailure;
import com.framework.apiserver.dto.dashboard.WeeklySummaryResponse;
import com.framework.apiserver.entity.TestRunInfoEntity;
import com.framework.apiserver.repository.TestRunInfoRepository;
import com.framework.apiserver.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Controller for handling dashboard-related API endpoints.
 *
 * <p>This controller provides endpoints to retrieve test run information,
 * including all runs, latest runs, statistics, weekly summaries, pass/fail pie charts,
 * and top failures.</p>
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    private final TestRunInfoRepository repository;

    /**
     * Retrieves all test runs sorted by start time in descending order.
     *
     * @return A list of all test runs.
     */
    @Operation(
            summary = "Get all test runs",
            description = "Retrieves all test runs sorted by start time in descending order.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all test runs retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/runs")
    public List<TestRunInfoEntity> getAllRuns() {
        return dashboardService.getAllRunsInfo();
    }

    /**
     * Retrieves the latest test runs, limited by the specified count.
     *
     * @param count The maximum number of test runs to retrieve (default is 5).
     * @return A list of the latest test runs.
     */
    @Operation(
            summary = "Get latest test runs",
            description = "Retrieves the latest test runs, limited by the specified count.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of latest test runs retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/latest")
    public List<TestRunInfoEntity> getLatestRuns(@RequestParam(defaultValue = "5") int count) {
        return dashboardService.getLatestRunsInfo(count);
    }



    /**
     * Retrieves statistics for test runs from the last 7 days.
     *
     * @return A map containing the statistics.
     */
    @Operation(
            summary = "Get test run statistics",
            description = "Retrieves statistics for test runs from the last 7 days, including total runs, passed runs, failed runs, and pass rate.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return dashboardService.getStatsLast7Days();
    }

    /**
     * Retrieves details of a specific test run by its run ID.
     *
     * @param runId The unique identifier of the test run.
     * @return A ResponseEntity containing the test run details if found, or a 404 status if not found.
     */
    @Operation(
            summary = "Get test run details",
            description = "Retrieves details of a specific test run by its run ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Test run details retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Test run not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/runs/{runId}")
    public ResponseEntity<TestRunInfoEntity> getRunById(@PathVariable String runId) {
        return dashboardService.getTestRunInfoByRunId(runId);
    }

    /**
     * Retrieves a weekly summary of test runs.
     *
     * @return A WeeklySummaryResponse object containing the weekly summary.
     */
    @Operation(
            summary = "Get weekly summary",
            description = "Retrieves a weekly summary of test runs.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Weekly summary retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/weekly-summary")
    public WeeklySummaryResponse getWeeklySummary() {
        return dashboardService.getWeeklySummary();
    }

    /**
     * Retrieves pass/fail statistics as a pie chart response.
     *
     * @return A PassFailPieResponse object containing pass/fail statistics.
     */
    @Operation(
            summary = "Get pass/fail pie chart",
            description = "Retrieves pass/fail statistics as a pie chart response.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Pass/fail statistics retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/pass-fail-pie")
    public PassFailPieResponse getPassFailPie() {
        return dashboardService.getPassFailStats();
    }

    /**
     * Retrieves the top failures in test runs.
     *
     * @return A list of TopFailure objects representing the top failures.
     */
    @Operation(
            summary = "Get top failures",
            description = "Retrieves the top failures in test runs.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Top failures retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/top-failures")
    public List<TopFailure> getTopFailures() {
        return dashboardService.getTopFailures();
    }
}