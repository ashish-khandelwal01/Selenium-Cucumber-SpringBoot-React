package com.framework.apiserver.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.framework.apiserver.config.KafkaProperties;
import com.framework.apiserver.dto.*;
import com.framework.apiserver.service.impl.TestExecutionServiceImpl;
import com.framework.apiserver.service.impl.TestRerunServiceImpl;
import com.framework.apiserver.utilities.AsyncJobManager;
import com.framework.apiserver.utilities.CommonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaConsumer {

    private final TestExecutionServiceImpl testExecutionService;
    private final TestRerunServiceImpl testRerunService;
    private final AsyncJobManager asyncJobManager;
    private final JobTrackingService jobTrackingService;
    private final BrowserContextManager browserContextManager;
    private final TestRunInfoService testRunInfoService;
    private final CommonUtils commonUtils;
    private final KafkaProducer kafkaProducerService;
    private final KafkaProperties kafkaProperties;

    /**
     * Consumes test execution requests from Kafka
     */
    @KafkaListener(topics = "${app.kafka.topics.test-execution}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeTestExecutionRequest(@Payload TestExecutionRequest request,
                                            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                            @Header(KafkaHeaders.OFFSET) long offset,
                                            Acknowledgment acknowledgment) {

        log.info("Consumed test execution request from topic: {}, partition: {}, offset: {}, jobId: {}",
                topic, partition, offset, request.getJobId());

        try {
            // Update job status to running
            asyncJobManager.setJobRunning(request.getJobId());

            // Send status update
            TestRunEvent startedEvent = new TestRunEvent(
                    request.getJobId(),
                    request.getRunId(),
                    TestRunEventType.TEST_EXECUTION_STARTED,
                    request.getTag(),
                    request.getCreatedBy()
            );
            kafkaProducerService.sendTestResult(startedEvent);

            // Execute the actual test
            TestExecutionResponse response = executeTest(request);

            // Complete the job
            asyncJobManager.completeJob(request.getJobId(), response);

            // Send completion status
            TestRunEvent completedEvent = new TestRunEvent(
                    request.getJobId(),
                    response.getRunId(),
                    TestRunEventType.TEST_EXECUTION_COMPLETED,
                    request.getTag(),
                    request.getCreatedBy()
            );
            kafkaProducerService.sendTestResult(completedEvent);

            // Acknowledge the message only after successful processing
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process test execution request for jobId: {}", request.getJobId(), e);

            // Handle retry logic
            handleRetry(request, e, acknowledgment);
        }
    }

    /**
     * Consumes test rerun requests from Kafka
     */
    @KafkaListener(topics = "${app.kafka.topics.test-rerun}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consumeTestRerunRequest(@Payload TestRerunRequest request,
                                        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
                                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                        @Header(KafkaHeaders.OFFSET) long offset,
                                        Acknowledgment acknowledgment) {

        log.info("Consumed test rerun request from topic: {}, partition: {}, offset: {}, jobId: {}, failedOnly: {}",
                topic, partition, offset, request.getJobId(), request.isRerunFailedOnly());

        try {
            // Update job status to running
            asyncJobManager.setJobRunning(request.getJobId());

            // Send status update
            TestRunEvent startedEvent = new TestRunEvent(
                    request.getJobId(),
                    request.getRunId(),
                    TestRunEventType.TEST_EXECUTION_STARTED,
                    request.getTag(),
                    request.getCreatedBy()
            );
            kafkaProducerService.sendTestResult(startedEvent);

            // Execute the rerun
            TestExecutionResponse response = executeRerun(request);

            // Complete the job
            asyncJobManager.completeJob(request.getJobId(), response);

            // Send completion status
            TestRunEvent completedEvent = new TestRunEvent(
                    request.getJobId(),
                    response.getRunId(),
                    TestRunEventType.TEST_EXECUTION_COMPLETED,
                    request.getTag(),
                    request.getCreatedBy()
            );
            kafkaProducerService.sendTestResult(completedEvent);

            // Acknowledge the message
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Failed to process test rerun request for jobId: {}", request.getJobId(), e);
            handleRetry(request, e, acknowledgment);
        }
    }

    /**
     * Execute the actual test
     */
    private TestExecutionResponse executeTest(TestExecutionRequest request) {
        String runId = CommonUtils.generateRunId();
        log.info("Executing test with runId: {}, tag: {}", runId, request.getTag());

        LocalDateTime startTime = LocalDateTime.now();

        try {
            // Execute the test
            CommonUtils.testCaseRun(request.getTag(), runId, Path.of("."), request.getBrowserType());

            LocalDateTime endTime = LocalDateTime.now();
            long durationSeconds = Duration.between(startTime, endTime).getSeconds();

            // Create run info and save to database
            HashMap<String, Object> result = commonUtils.createRunInfoFileAndDb(
                    testRunInfoService,
                    request.getTag(),
                    runId,
                    startTime,
                    endTime,
                    durationSeconds
            );

            return new TestExecutionResponse(
                    String.valueOf(result.get("status")),
                    (Integer) result.get("failureCount"),
                    runId
            );

        } catch (Exception e) {
            log.error("Test execution failed for runId: {}", runId, e);
            return new TestExecutionResponse("Execution Failed: " + e.getMessage(), -1, null);
        }
    }

    /**
     * Execute test rerun
     */
    private TestExecutionResponse executeRerun(TestRerunRequest request) {
        try {
            if (request.isRerunFailedOnly()) {
                return executeFailedRerun(request);
            } else {
                return executeAllRerun(request);
            }
        } catch (Exception e) {
            log.error("Test rerun failed for originalRunId: {}", request.getOriginalRunId(), e);
            return new TestExecutionResponse("Rerun Failed: " + e.getMessage(), -1, null);
        }
    }

    /**
     * Execute rerun of all tests
     */
    private TestExecutionResponse executeAllRerun(TestRerunRequest request) throws Exception {
        String originalRunId = request.getOriginalRunId();

        File infoFile = new File("reports/" + originalRunId + "/run-info.json");
        if (!infoFile.exists()) {
            throw new RuntimeException("Run ID not found: " + originalRunId);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(infoFile);
        String tags = node.get("tags").asText();

        // Create a new test execution request
        TestExecutionRequest newRequest = new TestExecutionRequest(
                request.getJobId(),
                request.getRunId(),
                tags,
                request.getCreatedBy(),
                browserContextManager.getBrowserType()
        );

        return executeTest(newRequest);
    }

    /**
     * Execute rerun of failed tests only
     */
    private TestExecutionResponse executeFailedRerun(TestRerunRequest request) throws Exception {
        String originalRunId = request.getOriginalRunId();

        List<String> failedScenarioPathsWithLines = testRunInfoService.getFailureScenarios(originalRunId);
        if (failedScenarioPathsWithLines.isEmpty()) {
            return new TestExecutionResponse("No failed scenarios found for runId " + originalRunId, 0, null);
        }

        String newRunId = CommonUtils.generateRunId();
        LocalDateTime startTime = LocalDateTime.now();

        Path rerunFilePath = Paths.get("reports/" + originalRunId + "/rerun.txt");
        Files.write(rerunFilePath, failedScenarioPathsWithLines);

        CommonUtils.testCaseRun(null, newRunId, rerunFilePath, browserContextManager.getBrowserType());
        commonUtils.deleteFile(rerunFilePath.toString());

        LocalDateTime endTime = LocalDateTime.now();
        long durationSeconds = Duration.between(startTime, endTime).getSeconds();

        HashMap<String, Object> result = commonUtils.createRunInfoFileAndDb(
                testRunInfoService,
                "Rerun",
                newRunId,
                startTime,
                endTime,
                durationSeconds
        );

        return new TestExecutionResponse(
                String.valueOf(result.get("status")),
                (Integer) result.get("failureCount"),
                newRunId
        );
    }

    /**
     * Handle retry logic for failed messages
     */
    private void handleRetry(TestRunEvent event, Exception error, Acknowledgment acknowledgment) {
        if (event.getRetryCount() < event.getMaxRetries()) {
            // Increment retry count
            event.setRetryCount(event.getRetryCount() + 1);

            log.warn("Retrying message processing for jobId: {}, attempt: {}/{}",
                    event.getJobId(), event.getRetryCount(), event.getMaxRetries());

            try {
                // Add exponential backoff
                Thread.sleep(kafkaProperties.getRetryBackoffDelay() * event.getRetryCount());

                // Re-queue the message (you might want to send to a retry topic)
                if (event instanceof TestExecutionRequest) {
                    kafkaProducerService.sendTestExecutionRequest((TestExecutionRequest) event);
                } else if (event instanceof TestRerunRequest) {
                    kafkaProducerService.sendTestRerunRequest((TestRerunRequest) event);
                }

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                log.error("Interrupted during retry backoff", ie);
            } catch (Exception e) {
                log.error("Failed to retry message for jobId: {}", event.getJobId(), e);
            }
        } else {
            // Max retries exceeded, fail the job
            log.error("Max retries exceeded for jobId: {}, failing job", event.getJobId());
            asyncJobManager.failJob(event.getJobId(), "Max retries exceeded: " + error.getMessage());

            // Send failure event
            TestRunEvent failedEvent = new TestRunEvent(
                    event.getJobId(),
                    event.getRunId(),
                    TestRunEventType.TEST_EXECUTION_FAILED,
                    event.getTag(),
                    event.getCreatedBy()
            );
            kafkaProducerService.sendTestResult(failedEvent);
        }

        // Acknowledge the message to remove it from the queue
        acknowledgment.acknowledge();
    }
}