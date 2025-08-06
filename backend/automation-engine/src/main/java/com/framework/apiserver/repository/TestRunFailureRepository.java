package com.framework.apiserver.repository;

import com.framework.apiserver.dto.RunFailures;
import com.framework.apiserver.entity.TestRunInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRunFailureRepository extends JpaRepository<TestRunInfoEntity, Long>,
        PagingAndSortingRepository<TestRunInfoEntity, Long> {

    @Query("""
            SELECT new com.framework.apiserver.dto.RunFailures(tri.runId, scenario)
            FROM TestRunInfoEntity tri
            JOIN tri.failureScenarios scenario
            ORDER BY tri.runId
            """)
    List<RunFailures> findAllTestRunFailures();
}
