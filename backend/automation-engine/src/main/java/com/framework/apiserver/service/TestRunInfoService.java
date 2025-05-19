package com.framework.apiserver.service;

import com.framework.apiserver.entity.TestRunInfoEntity;
import com.framework.apiserver.repository.TestRunInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class TestRunInfoService {

    private final TestRunInfoRepository repository;

    public void save(TestRunInfoEntity entity) {
        repository.save(entity);
    }

    public List<String> getFailureScenarios(String runId) {
        return repository.findByRunId(runId)
                .map(TestRunInfoEntity::getFailureScenarios)
                .orElse(Collections.emptyList());
    }
}
