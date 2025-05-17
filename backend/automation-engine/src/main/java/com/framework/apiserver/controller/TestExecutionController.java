package com.framework.apiserver.controller;

import com.framework.apiserver.dto.TestExecutionResponse;
import com.framework.apiserver.service.TestExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tests")
public class TestExecutionController {

    @Autowired
    private TestExecutionService testExecutionService;

    @PostMapping("/run")
    public TestExecutionResponse runTests(@RequestParam(defaultValue = "") String tags) {
        return testExecutionService.runCucumberTests(tags);
    }

}
