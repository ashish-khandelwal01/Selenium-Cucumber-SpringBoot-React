package com.framework.apiserver.dto.dashboard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatsDto {
    private double averageExecutionTime;
    private long failedToday;
}
