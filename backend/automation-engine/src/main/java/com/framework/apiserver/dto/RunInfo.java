package com.framework.apiserver.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class RunInfo {
    private String runId;
    private String tags;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;
    private String status;
    private int total;
    private int passed;
    private int failed;

}
