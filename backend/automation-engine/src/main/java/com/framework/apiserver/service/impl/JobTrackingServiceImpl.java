package com.framework.apiserver.service.impl;

import com.framework.apiserver.config.JobStatus;
import com.framework.apiserver.config.JobType;
import com.framework.apiserver.dto.JobStatusSummary;
import com.framework.apiserver.entity.JobTracking;
import com.framework.apiserver.event.JobStatusChangedEvent;
import com.framework.apiserver.repository.JobTrackingRepository;
import com.framework.apiserver.service.JobTrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Service implementation for managing job tracking operations.
 * Provides methods to start, update, cancel, and retrieve job details.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JobTrackingServiceImpl implements JobTrackingService {

    private final JobTrackingRepository jobTrackingRepository;
    private final ApplicationEventPublisher eventPublisher;

    // List to manage active SSE emitters for real-time updates
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final Set<SseEmitter> activeEmitters = ConcurrentHashMap.newKeySet();

    // List of statuses considered as active
    private static final List<JobStatus> ACTIVE_STATUSES = List.of(
            JobStatus.PENDING, JobStatus.RUNNING
    );

    // List of statuses considered as completed
    private static final List<JobStatus> COMPLETED_STATUSES = List.of(
            JobStatus.COMPLETED, JobStatus.FAILED, JobStatus.CANCELLED
    );

    /**
     * Creates a new Server-Sent Events (SSE) emitter for real-time job status updates.
     * Sends the current job status to the client immediately upon connection.
     *
     * @return A new SseEmitter instance for the client.
     */
    @Override
    public SseEmitter createSseEmitter() {
        SseEmitter emitter = new SseEmitter(300000L); // 5 minute timeout instead of infinite
        activeEmitters.add(emitter);
        emitters.add(emitter);

        Runnable cleanup = () -> {
            activeEmitters.remove(emitter);
            emitters.remove(emitter);
            log.debug("SSE client cleanup. Active connections: {}", emitters.size());
        };

        // Set up cleanup handlers first
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(throwable -> {
            log.debug("SSE client error: {}", throwable.getMessage());
            cleanup.run();
        });

        SecurityContext securityContext = SecurityContextHolder.getContext();

        CompletableFuture.supplyAsync(() -> {
            SecurityContextHolder.setContext(securityContext);
            try {
                return this.getInitialJobStatusData();
            } catch (Exception e) {
                log.error("Error getting initial job status: {}", e.getMessage(), e);
                return null;
            }
        }).thenAcceptAsync(currentStatus -> { // Use thenAcceptAsync to avoid blocking
            if (currentStatus != null && activeEmitters.contains(emitter)) {
                try {
                    emitter.send(SseEmitter.event()
                            .name("job-status-update")
                            .data(currentStatus));
                    log.info("New SSE client connected. Current active jobs: {}",
                            currentStatus.get("totalActiveJobs"));
                } catch (Exception e) {
                    log.error("Failed to send initial data to SSE client: {}", e.getMessage());
                    cleanup.run();
                }
            }
        }).exceptionally(throwable -> {
            log.error("Error in async SSE initialization: {}", throwable.getMessage(), throwable);
            cleanup.run();
            return null;
        });

        return emitter;
    }
    /**
     * Separate method to get initial job status data with proper transaction management.
     * This method is called asynchronously to prevent connection leaks.
     */
    @Transactional(readOnly = true, timeout = 5, propagation = Propagation.REQUIRES_NEW)
    public Map<String, Object> getInitialJobStatusData() {
        try {
            JobStatusSummary summary = getJobStatusSummaryInternal();
            Map<String, Object> data = new HashMap<>();
            data.put("totalActiveJobs", summary.getTotalActiveJobs());
            data.put("asyncJobs", summary.getAsyncJobs());
            data.put("syncJobs", summary.getSyncJobs());
            data.put("timestamp", LocalDateTime.now().toString());
            return data;
        } catch (Exception e) {
            log.error("Error in getInitialJobStatusData: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Internal method for getting job status summary - separated to avoid transaction conflicts.
     */
    private JobStatusSummary getJobStatusSummaryInternal() {
        List<JobTracking> activeJobs = jobTrackingRepository.findActiveJobsOrderByStartTime(ACTIVE_STATUSES);

        Map<JobType, Long> jobsByType = activeJobs.stream()
                .collect(Collectors.groupingBy(JobTracking::getType, Collectors.counting()));

        return JobStatusSummary.builder()
                .totalActiveJobs(activeJobs.size())
                .asyncJobs(jobsByType.getOrDefault(JobType.ASYNC, 0L).intValue())
                .syncJobs(jobsByType.getOrDefault(JobType.SYNC, 0L).intValue())
                .activeJobs(activeJobs)
                .build();
    }

    /**
     * Broadcasts job status updates to all connected SSE clients.
     * Removes any emitters that fail to receive the update.
     */
    @Override
    public void broadcastJobUpdate(JobTracking jobTracking) {
        if (emitters.isEmpty()) {
            return;
        }

        Map<String, Object> jobData = new HashMap<>();
        jobData.put("type", "job");
        jobData.put("jobId", jobTracking.getJobId());
        jobData.put("status", jobTracking.getStatus());
        jobData.put("errorMessage", jobTracking.getErrorMessage());
        jobData.put("timestamp", LocalDateTime.now().toString());

        // Use async processing for broadcast as well to prevent connection leaks
        CompletableFuture.supplyAsync(() -> {
            try {
                return this.getInitialJobStatusData();
            } catch (Exception e) {
                log.error("Error getting job status for broadcast: {}", e.getMessage(), e);
                return null;
            }
        }).thenAcceptAsync(summaryData -> {
            // Create a copy of active emitters to avoid concurrent modification
            Set<SseEmitter> currentEmitters = new HashSet<>(activeEmitters);
            List<SseEmitter> deadEmitters = new ArrayList<>();

            for (SseEmitter emitter : currentEmitters) {
                try {
                    // Send job update
                    emitter.send(SseEmitter.event()
                            .name("job-status-update")
                            .data(jobData));

                    // Send summary update if available
                    if (summaryData != null) {
                        Map<String, Object> summaryEvent = new HashMap<>(summaryData);
                        summaryEvent.put("type", "summary");
                        emitter.send(SseEmitter.event()
                                .name("job-status-update")
                                .data(summaryEvent));
                    }
                } catch (Exception e) {
                    log.debug("Failed to send update to SSE client: {}", e.getMessage());
                    deadEmitters.add(emitter);
                }
            }

            // Clean up dead emitters
            if (!deadEmitters.isEmpty()) {
                activeEmitters.removeAll(deadEmitters);
                emitters.removeAll(deadEmitters);
                log.debug("Removed {} dead SSE emitters. Active connections: {}",
                        deadEmitters.size(), emitters.size());
            }
        }).exceptionally(throwable -> {
            log.error("Error in broadcast async processing: {}", throwable.getMessage(), throwable);
            return null;
        });
    }

    /**
     * Starts an asynchronous job with the provided details.
     *
     * @param runId The run ID associated with the job.
     * @param tag The tag associated with the job.
     * @param createdBy The user or system that created the job.
     * @return The unique identifier of the started job.
     */
    @Override
    @Transactional(timeout = 5)
    public String startAsyncJob(String runId, String tag, String createdBy) {
        String jobId = UUID.randomUUID().toString();

        JobTracking jobTracking = JobTracking.builder()
                .jobId(jobId)
                .runId(runId)
                .type(JobType.ASYNC)
                .tag(tag)
                .status(JobStatus.PENDING)
                .startTime(LocalDateTime.now())
                .createdBy(createdBy)
                .build();

        jobTrackingRepository.save(jobTracking);
        eventPublisher.publishEvent(new JobStatusChangedEvent(jobTracking, "CREATED"));
        log.info("Started async job: {} for runId: {} with tag: {}", jobId, runId, tag);
        return jobId;
    }

    /**
     * Starts a synchronous job with the provided details.
     *
     * @param tag The tag associated with the job.
     * @param createdBy The user or system that created the job.
     * @return The unique identifier of the started job.
     */
    @Override
    @Transactional(timeout = 5)
    public String startSyncJob(String tag, String createdBy) {
        String jobId = UUID.randomUUID().toString();

        JobTracking jobTracking = JobTracking.builder()
                .jobId(jobId)
                .runId(tag) // For sync jobs, runId is same as tag
                .type(JobType.SYNC)
                .tag(tag)
                .status(JobStatus.RUNNING)
                .startTime(LocalDateTime.now())
                .createdBy(createdBy)
                .threadName(Thread.currentThread().getName())
                .build();

        jobTrackingRepository.save(jobTracking);
        eventPublisher.publishEvent(new JobStatusChangedEvent(jobTracking, "CREATED"));
        log.info("Started sync job: {} with tag: {}", jobId, tag);
        return jobId;
    }

    /**
     * Updates the status of a job.
     *
     * @param jobId The unique identifier of the job.
     * @param status The new status to set for the job.
     */
    @Override
    public void updateJobStatus(String jobId, JobStatus status) {
        updateJobStatus(jobId, status, null);
    }

    /**
     * Updates the status of a job with an optional error message.
     *
     * @param jobId The unique identifier of the job.
     * @param status The new status to set for the job.
     * @param errorMessage The error message, if any, associated with the job.
     */
    @Override
    @Transactional(timeout = 10, propagation = Propagation.REQUIRED)
    public void updateJobStatus(String jobId, JobStatus status, String errorMessage) {
        Optional<JobTracking> optionalJob = jobTrackingRepository.findById(jobId);

        if (optionalJob.isPresent()) {
            JobTracking jobTracking = optionalJob.get();
            JobStatus oldStatus = jobTracking.getStatus();

            jobTracking.setStatus(status);
            jobTracking.setErrorMessage(errorMessage);

            // Update thread name for running jobs
            if (status == JobStatus.RUNNING) {
                jobTracking.setThreadName(Thread.currentThread().getName());
            }

            // Set end time for completed jobs
            if (COMPLETED_STATUSES.contains(status)) {
                jobTracking.setEndTime(LocalDateTime.now());
            }

            jobTrackingRepository.save(jobTracking);
            eventPublisher.publishEvent(new JobStatusChangedEvent(jobTracking, "UPDATED"));
            log.info("Updated job {} status from {} to {}", jobId, oldStatus, status);
        } else {
            log.warn("Attempted to update non-existent job: {}", jobId);
        }
    }

    /**
     * Marks a job as completed with the specified status.
     *
     * @param jobId The unique identifier of the job.
     * @param status The status to set for the completed job.
     */
    @Override
    public void completeJob(String jobId, JobStatus status) {
        updateJobStatus(jobId, status);
    }

    /**
     * Marks a job as failed with the specified status.
     *
     * @param jobId The unique identifier of the job.
     * @param status The status to set for the completed job.
     * @param errorMessage An optional error message associated with the failure.
     */
    @Override
    public void failJob(String jobId, JobStatus status, String errorMessage) {
        updateJobStatus(jobId, status, errorMessage);
    }



    /**
     * Cancels a job with the specified job ID.
     *
     * @param jobId The unique identifier of the job to cancel.
     * @return True if the job was successfully cancelled, false otherwise.
     */
    @Override
    @Transactional(timeout = 10)
    public boolean cancelJob(String jobId) {
        Optional<JobTracking> optionalJob = jobTrackingRepository.findById(jobId);

        if (optionalJob.isPresent()) {
            JobTracking jobTracking = optionalJob.get();

            if (ACTIVE_STATUSES.contains(jobTracking.getStatus())) {
                updateJobStatus(jobId, JobStatus.CANCELLED);
                eventPublisher.publishEvent(new JobStatusChangedEvent(jobTracking, "CANCELLED"));

                log.info("Cancelled job: {}", jobId);
                return true;
            } else {
                log.warn("Cannot cancel job {} with status: {}", jobId, jobTracking.getStatus());
                return false;
            }
        }

        log.warn("Attempted to cancel non-existent job: {}", jobId);
        return false;
    }

    /**
     * Retrieves a summary of job statuses.
     *
     * @return A JobStatusSummary object containing the summary of active jobs.
     */
    @Override
    @Transactional(readOnly = true, timeout = 10)
    public JobStatusSummary getJobStatusSummary() {
        return getJobStatusSummaryInternal();
    }

    /**
     * Retrieves a list of all active jobs.
     *
     * @return A list of JobTracking entities representing active jobs.
     */
    @Override
    @Transactional(readOnly = true, timeout = 10)
    public List<JobTracking> getActiveJobs() {
        return jobTrackingRepository.findActiveJobsOrderByStartTime(ACTIVE_STATUSES);
    }

    /**
     * Retrieves a list of jobs filtered by a specific tag.
     *
     * @param tag The tag to filter jobs by.
     * @return A list of JobTracking entities matching the tag.
     */
    @Override
    @Transactional(readOnly = true, timeout = 10)
    public List<JobTracking> getJobsByTag(String tag) {
        return jobTrackingRepository.findByTagAndStatusIn(tag, ACTIVE_STATUSES);
    }

    /**
     * Retrieves a job by its unique identifier.
     *
     * @param jobId The unique identifier of the job.
     * @return An Optional containing the JobTracking entity if found, or empty if not found.
     */
    @Override
    @Transactional(readOnly = true, timeout = 10)
    public Optional<JobTracking> getJobById(String jobId) {
        return jobTrackingRepository.findById(jobId);
    }

    /**
     * Retrieves a list of jobs filtered by a specific run ID.
     *
     * @param runId The run ID to filter jobs by.
     * @return A list of JobTracking entities matching the run ID.
     */
    @Override
    @Transactional(readOnly = true, timeout = 10)
    public List<JobTracking> getJobsByRunId(String runId) {
        return jobTrackingRepository.findByRunIdAndStatusIn(runId, ACTIVE_STATUSES);
    }

    /**
     * Cleans up old completed jobs periodically.
     * Deletes jobs with completed statuses that ended more than 24 hours ago.
     * This method is scheduled to run every hour.
     */
    @Override
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional(timeout = 10)
    public void cleanupOldJobs() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);

        try {
            jobTrackingRepository.deleteByStatusInAndEndTimeBefore(COMPLETED_STATUSES, cutoffTime);
            log.debug("Cleaned up old completed jobs before: {}", cutoffTime);
        } catch (Exception e) {
            log.error("Error cleaning up old jobs", e);
        }
    }
}