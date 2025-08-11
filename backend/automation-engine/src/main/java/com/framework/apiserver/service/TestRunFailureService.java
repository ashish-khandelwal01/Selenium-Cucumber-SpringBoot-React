package com.framework.apiserver.service;

import com.framework.apiserver.dto.GroupRunFailures;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing test run failure-related operations.
 */
public interface TestRunFailureService {

    /**
     * Retrieves a map of grouped test run failures.
     *
     * @return A map where the key is a grouping criterion (e.g., failure type)
     *         and the value is a list of failure details.
     */
    Map<String, List<String>> getGroupedFailures();

    /**
     * Retrieves a paginated list of grouped test run failures.
     *
     * @param pageable The pagination information.
     * @return A Page object containing GroupRunFailures for the specified page.
     */
    Page<GroupRunFailures> getPaginatedFailures(Pageable pageable);

}