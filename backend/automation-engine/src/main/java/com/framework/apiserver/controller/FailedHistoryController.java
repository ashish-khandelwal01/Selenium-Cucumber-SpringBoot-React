package com.framework.apiserver.controller;

import com.framework.apiserver.service.TestExecutionService;
import com.framework.apiserver.service.TestRunFailureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class FailedHistoryController {

    @Autowired
    private TestRunFailureService testRunFailureService;
    @GetMapping("/failed-runs/pages")
    public Map<String, List<String>> getAllRunsPages() {
        return testRunFailureService.getGroupedFailures();
    }
}