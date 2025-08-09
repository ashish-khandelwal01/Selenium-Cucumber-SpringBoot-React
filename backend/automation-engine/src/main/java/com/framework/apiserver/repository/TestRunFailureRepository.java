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

@Repository
public interface TestRunFailureRepository extends JpaRepository<TestRunInfoEntity, Long>,
        PagingAndSortingRepository<TestRunInfoEntity, Long> {

    @Query("""
            SELECT new com.framework.apiserver.dto.RunFailures(tri.runId, scenario)
            FROM TestRunInfoEntity tri
            JOIN tri.failureScenarios scenario
            ORDER BY tri.startTime DESC
            """)
    List<RunFailures> findAllTestRunFailures();

    @Query("""
    SELECT tri.runId
    FROM TestRunInfoEntity tri
    ORDER BY tri.startTime DESC
    """)
    Page<String> findRunIds(Pageable pageable);

    @Query("""
    SELECT new com.framework.apiserver.dto.RunFailures(tri.runId, scenario)
    FROM TestRunInfoEntity tri
    JOIN tri.failureScenarios scenario
    WHERE tri.runId IN :runIds
    """)
    List<RunFailures> findFailuresByRunIds(@Param("runIds") List<String> runIds);
}
