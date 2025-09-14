package com.framework.apiserver.service;

import com.framework.apiserver.dto.dashboard.ReportStatsDto;
import org.springframework.http.ResponseEntity;

/**
 * Service interface for managing report-related operations.
 */
public interface ReportService {

    /**
     * Retrieves a ZIP file containing the report for a specific test run.
     *
     * @param runId The ID of the test run for which the report ZIP is to be retrieved.
     * @return A ResponseEntity containing the ZIP file or an appropriate error response.
     */
    ResponseEntity<?> getReportZip(String runId);

    /**
     * Retrieves a list of all available reports.
     *
     * @return A list of TestRunInfoEntity objects representing all reports.
     */
    ReportStatsDto listAllReports();

    /**
     * Retrieves the Spark report for a specific test run.
     *
     * @param runId The ID of the test run for which the Spark report is to be retrieved.
     * @return A ResponseEntity containing the Spark report or an appropriate error response.
     */
    ResponseEntity<?> viewSparkReport(String runId);
}