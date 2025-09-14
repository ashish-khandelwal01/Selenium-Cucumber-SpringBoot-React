package com.framework.apiserver.repository;

import com.framework.apiserver.dto.dashboard.PassFailProjection;
import com.framework.apiserver.entity.TestRunInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TestRunInfoRepository extends JpaRepository<TestRunInfoEntity, Long>,
        PagingAndSortingRepository<TestRunInfoEntity, Long> {
    List<TestRunInfoEntity> findByStartTimeAfter(LocalDateTime startTime);
    Optional<TestRunInfoEntity> findByRunId(String runId);
    List<TestRunInfoEntity> findTop5ByOrderByStartTimeDesc();

    @Query("SELECT AVG(t.durationSeconds) FROM TestRunInfoEntity t")
    Double findAverageDurationSeconds();

    @Query("""
        SELECT COUNT(t) 
        FROM TestRunInfoEntity t 
        WHERE t.status = :status 
          AND t.startTime >= :todayStart
    """)
    long countFailuresToday(@Param("status") String status, @Param("todayStart") LocalDateTime todayStart);

    @Query("SELECT SUM(t.passed) AS passed, SUM(t.failed) AS failed FROM TestRunInfoEntity t")
    PassFailProjection getPassFailStats();

}
