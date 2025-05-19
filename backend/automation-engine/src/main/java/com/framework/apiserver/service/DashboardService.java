package com.framework.apiserver.service;

import com.framework.apiserver.controller.DashboardController;
import com.framework.apiserver.dto.dashboard.PassFailPieResponse;
import com.framework.apiserver.dto.dashboard.TopFailure;
import com.framework.apiserver.dto.dashboard.WeeklySummaryResponse;
import com.framework.apiserver.entity.TestRunInfoEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DashboardService {

    WeeklySummaryResponse getWeeklySummary();
    PassFailPieResponse getPassFailStats();
    List<TopFailure> getTopFailures();
    ResponseEntity<TestRunInfoEntity> getTestRunInfoByRunId(String runId);
    Map<String, Object> getStatsLast7Days();
    List<TestRunInfoEntity> getLatestRunsInfo(int count);
    List<TestRunInfoEntity> getAllRunsInfo();
    Page<TestRunInfoEntity> getAllRunsInfo(Pageable pageable);

}
