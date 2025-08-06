package com.framework.apiserver.service.impl;

import com.framework.apiserver.dto.RunFailures;
import com.framework.apiserver.repository.TestRunFailureRepository;
import com.framework.apiserver.service.TestRunFailureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestRunFailureServiceImpl implements TestRunFailureService {

    @Autowired
    private TestRunFailureRepository testRunFailureRepository;

    public Map<String, List<String>> getGroupedFailures() {
        List<RunFailures> flatList = testRunFailureRepository.findAllTestRunFailures();
        return flatList.stream()
                .collect(Collectors.groupingBy(
                        RunFailures::getRunId,
                        Collectors.mapping(RunFailures::getScenario, Collectors.toList())
                ));
    }
}
