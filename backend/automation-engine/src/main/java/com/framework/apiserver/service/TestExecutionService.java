package com.framework.apiserver.service;

import com.framework.apiserver.dto.TestExecutionResponse;

public interface TestExecutionService {

    TestExecutionResponse runCucumberTests(String tag) ;

}