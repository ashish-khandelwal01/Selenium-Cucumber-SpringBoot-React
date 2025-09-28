package com.framework.apiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TestRerunRequest extends TestRunEvent {
    private String originalRunId;
    private boolean rerunFailedOnly;

    public TestRerunRequest(String jobId, String runId, String originalRunId, String createdBy, boolean rerunFailedOnly) {
        super(jobId, runId, rerunFailedOnly ? TestRunEventType.TEST_FAILED_RERUN_REQUESTED : TestRunEventType.TEST_RERUN_REQUESTED, "Rerun", createdBy);
        this.originalRunId = originalRunId;
        this.rerunFailedOnly = rerunFailedOnly;
    }
}