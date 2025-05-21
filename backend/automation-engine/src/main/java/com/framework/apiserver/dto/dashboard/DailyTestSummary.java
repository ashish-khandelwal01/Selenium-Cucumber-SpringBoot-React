package com.framework.apiserver.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyTestSummary {
    private LocalDate date;
    private int passed;
    private int failed;
}
