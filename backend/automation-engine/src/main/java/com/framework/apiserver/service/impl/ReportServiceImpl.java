package com.framework.apiserver.service.impl;

import com.framework.apiserver.dto.dashboard.ReportStatsDto;
import com.framework.apiserver.repository.TestRunInfoRepository;
import com.framework.apiserver.service.ReportService;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Service class for managing test reports.
 *
 * <p>This class provides methods to retrieve zipped report files, list metadata for all reports,
 * and view specific reports in the browser.</p>
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private TestRunInfoRepository testRunInfoRepository;

    private final ModelMapper modelMapper;

    @Autowired
    public ReportServiceImpl(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    private static final String REPORTS_BASE_PATH = "reports";

    /**
     * Retrieves the zipped report file for the specified run ID.
     *
     * <p>This method checks if the ZIP file for the given run ID exists in the reports directory.
     * If the file exists, it returns the file as a downloadable resource with appropriate headers.
     * If the file does not exist, it returns a 404 response. In case of an error while reading
     * the file, it returns a 500 response with the error message.</p>
     *
     * @param runId The unique identifier of the test run.
     * @return A ResponseEntity containing the ZIP file as a resource, or an error response.
     */
    public ResponseEntity<?> getReportZip(String runId) {
        File zipFile = new File(REPORTS_BASE_PATH, runId + ".zip");

        if (!zipFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Report ZIP file not found for runId: " + runId);
        }

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(zipFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentDisposition(ContentDisposition.attachment().filename(zipFile.getName()).build());
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(zipFile.length());

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error while reading report ZIP file: " + e.getMessage());
        }
    }

    /**
     * Report stats Dto returns average execution time and failure count today.
     *
     * <p>This method scans the reports directory and returns the
     * average time for execution and total count failed today.</p>
     *
     * @return A Report Stats Dto which returns the average and fail count today.
     */
    public ReportStatsDto listAllReports() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        Double avg = testRunInfoRepository.findAverageDurationSeconds();
        long failed = testRunInfoRepository.countFailuresToday("Failures", todayStart);

        return new ReportStatsDto(avg != null ? roundToTwoDecimals(avg) : 0.0, failed);
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    /**
     * Retrieves the SparkReport.html file for the specified run ID.
     *
     * <p>This method checks if the `SparkReport.html` file exists in the directory for the given run ID.
     * If the file exists, it returns the file as an inline resource with appropriate headers.
     * If the file does not exist, it returns a 404 response. In case of an error while reading
     * the file, it returns a 500 response with the error message.</p>
     *
     * @param runId The unique identifier of the test run.
     * @return A ResponseEntity containing the HTML file as a resource, or an error response.
     */
    public ResponseEntity<?> viewSparkReport(@PathVariable String runId) {
        File reportFile = new File(REPORTS_BASE_PATH, runId + "/Reports/SparkReport.html");

        if (!reportFile.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("SparkReport.html not found for runId: " + runId);
        }

        try {
            InputStreamResource resource = new InputStreamResource(new FileInputStream(reportFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.TEXT_HTML);
            headers.setContentDisposition(ContentDisposition.inline().filename("SparkReport.html").build());

            return new ResponseEntity<>(resource, headers, HttpStatus.OK);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to load report: " + e.getMessage());
        }
    }
}