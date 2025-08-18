package com.framework.apiserver.controller;

import com.framework.apiserver.dto.JobStatusSummary;
import com.framework.apiserver.entity.JobTracking;
import com.framework.apiserver.service.JobTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing job tracking operations.
 * Provides endpoints for retrieving job statuses, active jobs, and managing job lifecycle actions.
 */
@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class JobTrackingController {

    @Autowired
    private JobTrackingService jobTrackingService;

    @Operation(
            summary = "Get real-time job updates via Server-Sent Events",
            description = "Establishes an SSE connection for real-time job status updates",
            responses = {
                    @ApiResponse(responseCode = "200", description = "SSE connection established"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping(value = "/updates", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter getJobUpdates() {
        return jobTrackingService.createSseEmitter();
    }

    @Operation(
            summary = "Get job status summary",
            description = "Retrieves a summary of job statuses, including counts of active, async, and sync jobs.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job status summary retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/status")
    public ResponseEntity<JobStatusSummary> getJobStatus() {
        return ResponseEntity.ok(jobTrackingService.getJobStatusSummary());
    }

    @Operation(
            summary = "Get all active jobs",
            description = "Retrieves a list of all active jobs.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Active jobs retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/active")
    public ResponseEntity<List<JobTracking>> getActiveJobs() {
        return ResponseEntity.ok(jobTrackingService.getActiveJobs());
    }

    @Operation(
            summary = "Get job by ID",
            description = "Retrieves a job by its unique identifier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job retrieved successfully"),
                    @ApiResponse(responseCode = "404", description = "Job not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/{jobId}")
    public ResponseEntity<JobTracking> getJobById(@PathVariable String jobId) {
        Optional<JobTracking> job = jobTrackingService.getJobById(jobId);
        return job.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Get jobs by tag",
            description = "Retrieves a list of jobs filtered by a specific tag.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Jobs by tag retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/by-tag/{tag}")
    public ResponseEntity<List<JobTracking>> getJobsByTag(@PathVariable String tag) {
        return ResponseEntity.ok(jobTrackingService.getJobsByTag(tag));
    }

    @Operation(
            summary = "Get jobs by run ID",
            description = "Retrieves a list of jobs filtered by a specific run ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Jobs by run ID retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping("/by-run/{runId}")
    public ResponseEntity<List<JobTracking>> getJobsByRunId(@PathVariable String runId) {
        return ResponseEntity.ok(jobTrackingService.getJobsByRunId(runId));
    }

    @Operation(
            summary = "Cancel job",
            description = "Cancels a job with the specified job ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Job cancelled successfully with boolean status"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @PostMapping("/{jobId}/cancel")
    public ResponseEntity<Boolean> cancelJob(@PathVariable String jobId) {
        boolean cancelled = jobTrackingService.cancelJob(jobId);
        return ResponseEntity.ok(cancelled);
    }
}