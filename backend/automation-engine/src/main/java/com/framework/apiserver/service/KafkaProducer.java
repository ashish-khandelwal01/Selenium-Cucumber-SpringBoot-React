package com.framework.apiserver.service;

import com.framework.apiserver.config.KafkaProperties;
import com.framework.apiserver.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProperties kafkaProperties;

    /**
     * Sends a test execution request to Kafka
     */
    public CompletableFuture<SendResult<String, Object>> sendTestExecutionRequest(TestExecutionRequest request) {
        log.info("Sending test execution request for jobId: {}, tag: {}", request.getJobId(), request.getTag());

        return kafkaTemplate.send(kafkaProperties.getTestExecutionTopic(), request.getJobId(), request)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent test execution request for jobId: {} to partition: {} with offset: {}",
                                request.getJobId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send test execution request for jobId: {}", request.getJobId(), ex);
                    }
                });
    }

    /**
     * Sends a test rerun request to Kafka
     */
    public CompletableFuture<SendResult<String, Object>> sendTestRerunRequest(TestRerunRequest request) {
        log.info("Sending test rerun request for jobId: {}, originalRunId: {}, failedOnly: {}",
                request.getJobId(), request.getOriginalRunId(), request.isRerunFailedOnly());

        return kafkaTemplate.send(kafkaProperties.getTestRerunTopic(), request.getJobId(), request)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Successfully sent test rerun request for jobId: {} to partition: {} with offset: {}",
                                request.getJobId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    } else {
                        log.error("Failed to send test rerun request for jobId: {}", request.getJobId(), ex);
                    }
                });
    }

    /**
     * Sends test results/status updates
     */
    public CompletableFuture<SendResult<String, Object>> sendTestResult(TestRunEvent result) {
        log.debug("Sending test result for jobId: {}, status: {}", result.getJobId(), result.getEventType());

        return kafkaTemplate.send(kafkaProperties.getTestResultsTopic(), result.getJobId(), result)
                .whenComplete((sendResult, ex) -> {
                    if (ex == null) {
                        log.debug("Successfully sent test result for jobId: {}", result.getJobId());
                    } else {
                        log.error("Failed to send test result for jobId: {}", result.getJobId(), ex);
                    }
                });
    }

    /**
     * Generic method to send any event with retry logic
     */
    public CompletableFuture<SendResult<String, Object>> sendEventWithRetry(String topic, String key, Object event) {
        return sendWithRetry(topic, key, event, 0);
    }

    private CompletableFuture<SendResult<String, Object>> sendWithRetry(String topic, String key, Object event, int attempt) {
        return kafkaTemplate.send(topic, key, event)
                .handle((result, ex) -> {
                    if (ex != null && attempt < kafkaProperties.getMaxRetryAttempts()) {
                        log.warn("Failed to send message to topic: {}, attempt: {}, retrying...", topic, attempt + 1, ex);
                        try {
                            Thread.sleep(kafkaProperties.getRetryBackoffDelay() * (attempt + 1));
                            return sendWithRetry(topic, key, event, attempt + 1).join();
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new RuntimeException("Interrupted while retrying Kafka send", ie);
                        }
                    } else if (ex != null) {
                        log.error("Failed to send message to topic: {} after {} attempts", topic, attempt + 1, ex);
                        throw new RuntimeException("Failed to send Kafka message after retries", ex);
                    }
                    return result;
                });
    }

    /**
     * Check if Kafka is available
     */
    public boolean isKafkaAvailable() {
        try {
            kafkaTemplate.send("health-check", "ping", "test").get();
            return true;
        } catch (Exception e) {
            log.warn("Kafka health check failed", e);
            return false;
        }
    }
}