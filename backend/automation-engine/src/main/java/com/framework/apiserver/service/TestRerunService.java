package com.framework.apiserver.service;

import com.framework.apiserver.dto.TestExecutionResponse;

public interface  TestRerunService {
    TestExecutionResponse rerunAll(String runId);
    TestExecutionResponse rerunFailed(String runId);

    void rerunTestsAsync(String runId, String jobId);

    void rerunFailedTestsAsync(String runId, String jobId);
}
