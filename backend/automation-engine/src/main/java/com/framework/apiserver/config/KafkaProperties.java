package com.framework.apiserver.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Data
public class KafkaProperties {
    @Value("${app.kafka.enabled:true}")
    private boolean enabled;

    @Value("${app.kafka.fallback-to-threads:true}")
    private boolean fallbackToThreads;

    @Value("${app.kafka.topics.test-execution}")
    private String testExecutionTopic;

    @Value("${app.kafka.topics.test-rerun}")
    private String testRerunTopic;

    @Value("${app.kafka.topics.test-results}")
    private String testResultsTopic;

    @Value("${app.kafka.retry.max-attempts:3}")
    private int maxRetryAttempts;

    @Value("${app.kafka.retry.backoff-delay:2000}")
    private long retryBackoffDelay;
}