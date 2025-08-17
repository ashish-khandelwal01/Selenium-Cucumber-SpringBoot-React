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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
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

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    private static final List<JobStatus> ACTIVE_STATUSES = List.of(
            JobStatus.PENDING, JobStatus.RUNNING
    );

    private static final List<JobStatus> COMPLETED_STATUSES = List.of(
            JobStatus.COMPLETED, JobStatus.FAILED, JobStatus.CANCELLED
    );

    @Override
    public SseEmitter createSseEmitter() {
        SseEmitter emitter = new SseEmitter(0L); // No timeout
        emitters.add(emitter);

        // Send current job status immediately upon connection
        try {
            JobStatusSummary currentStatus = getJobStatusSummary();
            Map<String, Object> data = new HashMap<>();
            data.put("totalActiveJobs", currentStatus.getTotalActiveJobs());
            data.put("asyncJobs", currentStatus.getAsyncJobs());
            data.put("syncJobs", currentStatus.getSyncJobs());
            data.put("timestamp", LocalDateTime.now().toString());

            emitter.send(SseEmitter.event()
                    .name("job-status-update")
                    .data(data));

            log.info("New SSE client connected. Current active jobs: {}", currentStatus.getTotalActiveJobs());
        } catch (IOException e) {
            log.error("Failed to send initial data to new SSE client", e);
            emitters.remove(emitter);
        }

        // Handle cleanup when client disconnects
        emitter.onCompletion(() -> {
            emitters.remove(emitter);
            log.debug("SSE client disconnected. Active connections: {}", emitters.size());
        });

        emitter.onTimeout(() -> {
            emitters.remove(emitter);
            log.debug("SSE client timed out. Active connections: {}", emitters.size());
        });

        emitter.onError(throwable -> {
            emitters.remove(emitter);
            log.debug("SSE client error. Active connections: {}", emitters.size());
        });

        return emitter;
    }

    /**
     * Broadcasts job status updates to all connected SSE clients.
     */
    @Override
    public void broadcastJobUpdate() {
        if (emitters.isEmpty()) {
            return; // No clients connected
        }

        JobStatusSummary status = getJobStatusSummary();
        Map<String, Object> data = new HashMap<>();
        data.put("totalActiveJobs", status.getTotalActiveJobs());
        data.put("asyncJobs", status.getAsyncJobs());
        data.put("syncJobs", status.getSyncJobs());
        data.put("timestamp", LocalDateTime.now().toString());

        List<SseEmitter> deadEmitters = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("job-status-update")
                        .data(data));
            } catch (IOException e) {
                log.debug("Failed to send update to SSE client, removing from list", e);
                deadEmitters.add(emitter);
            }
        }

        // Remove dead emitters
        emitters.removeAll(deadEmitters);

        if (!deadEmitters.isEmpty()) {
            log.debug("Removed {} dead SSE connections. Active connections: {}",
                    deadEmitters.size(), emitters.size());
        }
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
    @Transactional
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
        broadcastJobUpdate();
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
    @Transactional
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
        broadcastJobUpdate();
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
    @Transactional
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
    @Transactional
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
            broadcastJobUpdate();
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
    @Transactional
    public void completeJob(String jobId, JobStatus status) {
        updateJobStatus(jobId, status);
    }

    /**
     * Cancels a job with the specified job ID.
     *
     * @param jobId The unique identifier of the job to cancel.
     * @return True if the job was successfully cancelled, false otherwise.
     */
    @Override
    @Transactional
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
    public JobStatusSummary getJobStatusSummary() {
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
     * Retrieves a list of all active jobs.
     *
     * @return A list of JobTracking entities representing active jobs.
     */
    @Override
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
    @Transactional
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