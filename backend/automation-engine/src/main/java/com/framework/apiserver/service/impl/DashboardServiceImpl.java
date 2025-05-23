package com.framework.apiserver.service.impl;

import com.framework.apiserver.dto.dashboard.DailyTestSummary;
import com.framework.apiserver.entity.TestRunInfoEntity;
import com.framework.apiserver.repository.TestRunInfoRepository;
import com.framework.apiserver.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import com.framework.apiserver.dto.dashboard.WeeklySummaryResponse;
import com.framework.apiserver.dto.dashboard.PassFailPieResponse;
import com.framework.apiserver.dto.dashboard.TopFailure;

/**
 * Implementation of the DashboardService interface.
 *
 * <p>This service provides methods to retrieve and process test run information,
 * including weekly summaries, pass/fail statistics, top failures, and test run details.</p>
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final TestRunInfoRepository repository;

    /**
     * Retrieves a summary of test runs for the current week.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *   <li>Determines the start of the current week (Monday).</li>
     *   <li>Fetches all test runs that started after the beginning of the week.</li>
     *   <li>Groups the test runs by their start date.</li>
     *   <li>Calculates the total passed and failed tests for each day of the week.</li>
     *   <li>Aggregates the daily summaries into a list and computes the weekly totals.</li>
     * </ul>
     *
     * @return A {@link WeeklySummaryResponse} object containing:
     *         <ul>
     *           <li>A list of daily test summaries for the current week.</li>
     *           <li>The total number of passed tests for the week.</li>
     *           <li>The total number of failed tests for the week.</li>
     *         </ul>
     */
    public WeeklySummaryResponse getWeeklySummary() {
        LocalDate startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDateTime weekStartDateTime = startOfWeek.atStartOfDay();

        // Get all test runs from this week
        List<TestRunInfoEntity> runs = repository.findByStartTimeAfter(weekStartDateTime);

        // Group by date
        Map<LocalDate, List<TestRunInfoEntity>> groupedByDate = runs.stream()
                .collect(Collectors.groupingBy(run -> run.getStartTime().toLocalDate()));

        List<DailyTestSummary> dailySummaries = new ArrayList<>();
        int totalPassed = 0;
        int totalFailed = 0;

        for (LocalDate date = startOfWeek; !date.isAfter(startOfWeek.plusDays(6)); date = date.plusDays(1)) {
            List<TestRunInfoEntity> dailyRuns = groupedByDate.getOrDefault(date, Collections.emptyList());

            int passed = dailyRuns.stream().mapToInt(TestRunInfoEntity::getPassed).sum();
            int failed = dailyRuns.stream().mapToInt(TestRunInfoEntity::getFailed).sum();

            totalPassed += passed;
            totalFailed += failed;

            dailySummaries.add(new DailyTestSummary(date, passed, failed));
            if(date.isEqual(LocalDate.now())) {
                break;
            }
        }

        return new WeeklySummaryResponse(dailySummaries, totalPassed, totalFailed);
    }


    /**
     * Retrieves pass/fail statistics for all test runs.
     *
     * @return A PassFailPieResponse object containing the total passed and failed test counts.
     */
    public PassFailPieResponse getPassFailStats() {
        List<TestRunInfoEntity> allRuns = repository.findAll();

        int passed = allRuns.stream().mapToInt(TestRunInfoEntity::getPassed).sum();
        int failed = allRuns.stream().mapToInt(TestRunInfoEntity::getFailed).sum();

        return new PassFailPieResponse(passed, failed);
    }

    /**
     * Retrieves the top 10 failure scenarios across all test runs.
     *
     * @return A list of TopFailure objects representing the most frequent failure scenarios.
     */
    public List<TopFailure> getTopFailures() {
        List<TestRunInfoEntity> allRuns = repository.findAll();

        Map<String, Integer> failureMap = new HashMap<>();

        for (TestRunInfoEntity run : allRuns) {
            List<String> failures = run.getFailureScenarios();
            if (failures != null) {
                for (String scenario : failures) {
                    failureMap.put(scenario, failureMap.getOrDefault(scenario, 0) + 1);
                }
            }
        }

        return failureMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .map(e -> new TopFailure(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves details of a specific test run by its run ID.
     *
     * @param runId The unique identifier of the test run.
     * @return A ResponseEntity containing the test run details if found, or a 404 status if not found.
     */
    public ResponseEntity<TestRunInfoEntity> getTestRunInfoByRunId(String runId) {
        return repository.findAll().stream()
                .filter(run -> runId.equals(run.getRunId()))
                .findFirst()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Retrieves statistics for test runs from the last 7 days.
     *
     * @return A map containing the total runs, passed runs, failed runs, and pass rate as a percentage.
     */
    public Map<String, Object> getStatsLast7Days() {
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        List<TestRunInfoEntity> recentRuns = repository.findByStartTimeAfter(since);

        long total = recentRuns.size();
        long passed = recentRuns.stream().filter(run -> "Execution Successful".equalsIgnoreCase(run.getStatus())).count();
        long failed = total - passed;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalRuns", total);
        stats.put("passed", passed);
        stats.put("failed", failed);
        stats.put("passRate", total > 0 ? (passed * 100.0 / total) : 0);

        return stats;
    }

    /**
     * Retrieves the latest test runs, limited by the specified count.
     *
     * @param count The maximum number of test runs to retrieve.
     * @return A list of the latest test runs.
     */
    public List<TestRunInfoEntity> getLatestRunsInfo(int count) {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "startTime")).stream()
                .limit(count)
                .toList();
    }

    /**
     * Retrieves all test runs sorted by start time in descending order.
     *
     * @return A list of all test runs.
     */
    @Override
    public List<TestRunInfoEntity> getAllRunsInfo() {
        return repository.findAll(Sort.by(Sort.Direction.DESC, "startTime"));
    }

    @Override
    public Page<TestRunInfoEntity> getAllRunsInfo(Pageable pageable) {
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "startTime")
        );
        return repository.findAll(sortedPageable);
    }
}