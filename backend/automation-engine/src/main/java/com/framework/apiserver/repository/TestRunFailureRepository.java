package com.framework.apiserver.repository;

import com.framework.apiserver.dto.RunFailures;
import com.framework.apiserver.entity.TestRunInfoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository interface for accessing test run failure data.
 * Extends JpaRepository and PagingAndSortingRepository to provide CRUD and pagination capabilities.
 */
@Repository
public interface TestRunFailureRepository extends JpaRepository<TestRunInfoEntity, Long>,
        PagingAndSortingRepository<TestRunInfoEntity, Long> {

    /**
     * Retrieves all test run failures, including their run IDs and associated scenarios.
     * The results are ordered by the start time of the test runs in descending order.
     *
     * @return A list of RunFailures DTOs containing run IDs and their associated scenarios.
     */
    @Query("""
            SELECT new com.framework.apiserver.dto.RunFailures(tri.runId, scenario)
            FROM TestRunInfoEntity tri
            JOIN tri.failureScenarios scenario
            ORDER BY tri.startTime DESC
            """)
    List<RunFailures> findAllTestRunFailures();

    /**
     * Retrieves a paginated list of test runs that have associated failure scenarios.
     * The results are ordered by the start time of the test runs in descending order.
     *
     * @param pageable The pagination information.
     * @return A page of TestRunInfoEntity objects representing test runs with failures.
     */
    @Query("""
    SELECT tri FROM TestRunInfoEntity tri
    WHERE size(tri.failureScenarios) > 0
    ORDER BY tri.startTime DESC
    """)
    Page<TestRunInfoEntity> findRunsWithFailures(Pageable pageable);

    /**
     * Retrieves all failure scenarios for a given list of run IDs.
     *
     * @param runIds A list of run IDs to filter the results.
     * @return A list of RunFailures DTOs containing run IDs and their associated scenarios.
     */
    @Query("""
    SELECT new com.framework.apiserver.dto.RunFailures(tri.runId, scenario)
    FROM TestRunInfoEntity tri
    JOIN tri.failureScenarios scenario
    WHERE tri.runId IN :runIds
    """)
    List<RunFailures> findFailuresByRunIds(@Param("runIds") List<String> runIds);

    /**
     * Retrieves raw failure data (run ID and scenario) for a given list of run IDs.
     *
     * @param runIds A list of run IDs to filter the results.
     * @return A list of Object arrays where each array contains a run ID and a scenario.
     */
    @Query("""
    SELECT tri.runId, scenario
    FROM TestRunInfoEntity tri
    JOIN tri.failureScenarios scenario
    WHERE tri.runId IN :runIds
    """)
    List<Object[]> testFailuresByRunIds(@Param("runIds") List<String> runIds);

}