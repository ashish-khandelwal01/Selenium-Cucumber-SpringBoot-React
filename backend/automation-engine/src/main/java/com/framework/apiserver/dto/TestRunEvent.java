package com.framework.apiserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRunEvent {
    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("jobId")
    private String jobId;

    @JsonProperty("runId")
    private String runId;

    @JsonProperty("eventType")
    private TestRunEventType eventType;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("createdBy")
    private String createdBy;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("metadata")
    private Map<String, Object> metadata;

    @JsonProperty("retryCount")
    private int retryCount = 0;

    @JsonProperty("maxRetries")
    private int maxRetries = 3;

    // Convenience constructors
    public TestRunEvent(String runId, TestRunEventType eventType) {
        this.runId = runId;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }

    public TestRunEvent(String jobId, String runId, TestRunEventType eventType, String tag, String createdBy) {
        this.jobId = jobId;
        this.runId = runId;
        this.eventType = eventType;
        this.tag = tag;
        this.createdBy = createdBy;
        this.timestamp = LocalDateTime.now();
        this.eventId = java.util.UUID.randomUUID().toString();
    }
}