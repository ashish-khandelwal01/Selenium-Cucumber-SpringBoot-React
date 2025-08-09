package com.framework.apiserver.service.impl;

import com.framework.apiserver.dto.GroupRunFailures;
import com.framework.apiserver.dto.RunFailures;
import com.framework.apiserver.repository.TestRunFailureRepository;
import com.framework.apiserver.service.TestRunFailureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TestRunFailureServiceImpl implements TestRunFailureService {

    @Autowired
    private TestRunFailureRepository testRunFailureRepository;

    @Override
    public Map<String, List<String>> getGroupedFailures() {
        List<RunFailures> flatList = testRunFailureRepository.findAllTestRunFailures();
        return flatList.stream()
                .collect(Collectors.groupingBy(
                        RunFailures::getRunId,
                        LinkedHashMap::new,
                        Collectors.mapping(RunFailures::getScenario, Collectors.toList())
                ));
    }

    @Override
    public Page<GroupRunFailures> getPaginatedFailures(Pageable pageable) {
        Page<String> runIdPage = testRunFailureRepository.findRunIds(pageable);
        List<String> runIds = runIdPage.getContent();

        if (runIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, runIdPage.getTotalElements());
        }

        List<RunFailures> flatFailures = testRunFailureRepository.findFailuresByRunIds(runIds);

        List<GroupRunFailures> grouped = flatFailures.stream()
                .collect(Collectors.groupingBy(
                        RunFailures::getRunId,
                        LinkedHashMap::new,
                        Collectors.mapping(RunFailures::getScenario, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> new GroupRunFailures(entry.getKey(), entry.getValue()))
                .toList();

        return new PageImpl<>(grouped, pageable, runIdPage.getTotalElements());
    }
}
