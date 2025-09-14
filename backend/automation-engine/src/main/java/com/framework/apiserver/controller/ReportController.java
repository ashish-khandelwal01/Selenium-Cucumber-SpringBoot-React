package com.framework.apiserver.controller;

import com.framework.apiserver.dto.dashboard.ReportStatsDto;
import com.framework.apiserver.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ReportController handles API endpoints related to test report management.
 *
 * <p>It provides endpoints for downloading report ZIPs, listing report metadata,
 * and viewing reports in the browser.</p>
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>ReportService for business logic related to reports</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * Downloads the zipped Cucumber Extent report for the given runId.
     *
     * @param runId The unique identifier of the test run.
     * @return A ResponseEntity containing the report ZIP file or an error response.
     */
    @Operation(
            summary = "Download test report ZIP by runId",
            description = "Downloads the zipped Cucumber Extent report for the given runId",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Report ZIP downloaded successfully"),
                    @ApiResponse(responseCode = "404", description = "Report not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/{runId}/download")
    public ResponseEntity<?> downloadReportZip(@PathVariable String runId) {
        return reportService.getReportZip(runId);
    }

    /**
     * Lists metadata for all available reports.
     *
     * @return A ResponseEntity containing a list of ReportMetaData objects.
     */
    @Operation(summary = "List all report metadata")
    @GetMapping("/list")
    public ReportStatsDto listReports() {
        return reportService.listAllReports();
    }

    /**
     * Displays the SparkReport.html for the given runId in the browser.
     *
     * @param runId The unique identifier of the test run.
     * @return A ResponseEntity containing the SparkReport.html or an error response.
     */
    @Operation(summary = "View SparkReport.html in browser")
    @GetMapping("/{runId}/view")
    public ResponseEntity<?> viewReport(@PathVariable String runId) {
        return reportService.viewSparkReport(runId);
    }

}