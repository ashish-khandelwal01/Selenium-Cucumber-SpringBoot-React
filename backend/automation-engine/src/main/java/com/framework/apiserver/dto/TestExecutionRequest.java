package com.framework.apiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TestExecutionRequest extends TestRunEvent {
    private String browserType;
    private boolean isAsync;

    public TestExecutionRequest(String jobId, String runId, String tag, String createdBy, String browserType) {
        super(jobId, runId, TestRunEventType.TEST_EXECUTION_REQUESTED, tag, createdBy);
        this.browserType = browserType;
        this.isAsync = true;
    }
}