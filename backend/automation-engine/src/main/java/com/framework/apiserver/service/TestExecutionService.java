package com.framework.apiserver.service;

import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.testrunner.TestRunner;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.springframework.stereotype.Service;

@Service
public class TestExecutionService {

    public TestExecutionResponse runCucumberTests(String tag) {
        try {
            System.setProperty("cucumber.filter.tags", tag);
            Result result = JUnitCore.runClasses(TestRunner.class);
            int failureCount = result.getFailureCount();
            System.out.println("Test execution completed with "+failureCount+ "failures.");
            String status = failureCount == 0 ? "Execution Successful" : "Execution Completed with Failures"+failureCount;
            return new TestExecutionResponse(status, failureCount);
        } catch (Exception e) {
            return new TestExecutionResponse("Execution Failed: " + e.getMessage(), -1);
        }
    }
}
