package com.framework.apiserver.repository;

import com.framework.apiserver.entity.TestRunInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TestRunInfoRepository extends JpaRepository<TestRunInfoEntity, Long> {
    List<TestRunInfoEntity> findByStartTimeAfter(LocalDateTime startTime);
}
