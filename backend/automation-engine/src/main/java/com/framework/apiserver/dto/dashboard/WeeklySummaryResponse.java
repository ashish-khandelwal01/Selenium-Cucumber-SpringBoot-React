package com.framework.apiserver.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeeklySummaryResponse {
    private List<DailyTestSummary> dailySummaries;
    private int totalPassed;
    private int totalFailed;
}
