package com.framework.apiserver.service;

import com.framework.apiserver.dto.dashboard.PassFailPieResponse;
import com.framework.apiserver.dto.dashboard.TopFailure;
import com.framework.apiserver.dto.dashboard.WeeklySummaryResponse;
import com.framework.apiserver.entity.TestRunInfoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing dashboard-related operations.
 */
public interface DashboardService {

    /**
     * Retrieves a summary of test runs for the current week.
     *
     * @return A WeeklySummaryResponse object containing the weekly summary data.
     */
    WeeklySummaryResponse getWeeklySummary();

    /**
     * Retrieves pass/fail statistics for test runs.
     *
     * @return A PassFailPieResponse object containing pass/fail statistics.
     */
    PassFailPieResponse getPassFailStats();

    /**
     * Retrieves a list of the top failure scenarios.
     *
     * @return A list of TopFailure objects representing the most frequent failures.
     */
    List<TopFailure> getTopFailures();

    /**
     * Retrieves detailed information about a specific test run by its run ID.
     *
     * @param runId The ID of the test run.
     * @return A ResponseEntity containing the TestRunInfoEntity for the specified run ID.
     */
    ResponseEntity<TestRunInfoEntity> getTestRunInfoByRunId(String runId);

    /**
     * Retrieves various statistics for the last 7 days.
     *
     * @return A map containing key-value pairs of statistics for the last 7 days.
     */
    Map<String, Object> getStatsLast7Days();

    /**
     * Retrieves information about the latest test runs, limited by a specified count.
     *
     * @param count The maximum number of test runs to retrieve.
     * @return A list of TestRunInfoEntity objects representing the latest test runs.
     */
    List<TestRunInfoEntity> getLatestRunsInfo(int count);

    /**
     * Retrieves information about all test runs.
     *
     * @return A list of TestRunInfoEntity objects representing all test runs.
     */
    List<TestRunInfoEntity> getAllRunsInfo();

    /**
     * Retrieves a paginated list of all test runs.
     *
     * @param pageable The pagination information.
     * @return A Page object containing TestRunInfoEntity objects for the specified page.
     */
    Page<TestRunInfoEntity> getAllRunsInfo(Pageable pageable);

}