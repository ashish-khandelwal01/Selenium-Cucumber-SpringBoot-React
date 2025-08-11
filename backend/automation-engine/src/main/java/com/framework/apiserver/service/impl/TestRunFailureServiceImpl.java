package com.framework.apiserver.service.impl;

import com.framework.apiserver.dto.GroupRunFailures;
import com.framework.apiserver.dto.RunFailures;
import com.framework.apiserver.entity.TestRunInfoEntity;
import com.framework.apiserver.repository.TestRunFailureRepository;
import com.framework.apiserver.service.TestRunFailureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of the TestRunFailureService interface.
 * Provides methods to retrieve and process test run failure data.
 */
@Service
public class TestRunFailureServiceImpl implements TestRunFailureService {

    @Autowired
    private TestRunFailureRepository testRunFailureRepository;

    /**
     * Retrieves all test run failures and groups them by run ID.
     *
     * @return A map where the key is the run ID and the value is a list of scenarios associated with that run ID.
     */
    @Override
    public Map<String, List<String>> getGroupedFailures() {
        // Fetch all test run failures from the repository
        List<RunFailures> flatList = testRunFailureRepository.findAllTestRunFailures();

        // Group the failures by run ID and collect the scenarios into a list
        return flatList.stream()
                .collect(Collectors.groupingBy(
                        RunFailures::getRunId,
                        LinkedHashMap::new,
                        Collectors.mapping(RunFailures::getScenario, Collectors.toList())
                ));
    }

    /**
     * Retrieves a paginated list of grouped test run failures.
     *
     * @param pageable The pagination information.
     * @return A page of GroupRunFailures objects, each containing a run ID and its associated scenarios.
     */
    @Override
    public Page<GroupRunFailures> getPaginatedFailures(Pageable pageable) {
        // Step 1: Get the page of TestRunInfoEntity containing run IDs
        Page<TestRunInfoEntity> runIdPage = testRunFailureRepository.findRunsWithFailures(pageable);

        // Step 2: Extract the list of run ID strings from the entities
        List<String> runIds = runIdPage.getContent().stream()
                .map(TestRunInfoEntity::getRunId)
                .toList();

        // Step 3: Return an empty page if no run IDs are found
        if (runIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, runIdPage.getTotalElements());
        }

        // Step 4: Fetch a flat list of RunFailures DTOs for the run IDs
        List<RunFailures> flatFailures = testRunFailureRepository.findFailuresByRunIds(runIds);
        // Step 5: Group the scenarios by run ID and map them to GroupRunFailures objects
        Map<String, List<RunFailures>> groupedByRunId = flatFailures.stream()
                .collect(Collectors.groupingBy(
                        RunFailures::getRunId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<GroupRunFailures> grouped = groupedByRunId.entrySet().stream()
                .map(entry -> {
                    String runId = entry.getKey();
                    List<RunFailures> failures = entry.getValue();

                    // Get the first failure to extract common fields (startTime, tags)
                    RunFailures firstFailure = failures.get(0);
                    LocalDateTime startTime = firstFailure.getStartTime();
                    String tags = firstFailure.getTags();

                    // Extract all scenarios for this runId
                    List<String> scenarios = failures.stream()
                            .map(RunFailures::getScenario)
                            .toList();

                    return new GroupRunFailures(runId, startTime, tags, scenarios);
                })
                .toList();

        // Step 6: Return a page containing the grouped failures
        return new PageImpl<>(grouped, pageable, runIdPage.getTotalElements());
    }
}