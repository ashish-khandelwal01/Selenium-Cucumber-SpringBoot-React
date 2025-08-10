package com.framework.apiserver.service;

import com.framework.apiserver.entity.TestRunInfoEntity;
import com.framework.apiserver.repository.TestRunInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

import java.util.Collections;

/**
 * Service class for managing test run information.
 */
@Service
@RequiredArgsConstructor
public class TestRunInfoService {

    private final TestRunInfoRepository repository;

    /**
     * Saves a TestRunInfoEntity to the database.
     *
     * @param entity The TestRunInfoEntity object to be saved.
     */
    public void save(TestRunInfoEntity entity) {
        repository.save(entity);
    }

    /**
     * Retrieves the failure scenarios for a given test run ID.
     *
     * @param runId The ID of the test run.
     * @return A list of failure scenarios associated with the given run ID, or an empty list if none are found.
     */
    public List<String> getFailureScenarios(String runId) {
        return repository.findByRunId(runId)
                .map(TestRunInfoEntity::getFailureScenarios)
                .orElse(Collections.emptyList());
    }
}