package com.framework.apiserver.service;

import com.framework.apiserver.dto.TestExecutionResponse;

public interface  TestRerunService {
    TestExecutionResponse rerunAll(String runId);
    TestExecutionResponse rerunFailed(String runId);
}
