package com.framework.apiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RunFailures {
    private String runId;
    private LocalDateTime startTime;
    private String tags;
    private String scenario;
}

