package com.framework.apiserver.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeeklySummaryResponse {
    private int totalRuns;
    private int totalPassed;
    private int totalFailed;
}
