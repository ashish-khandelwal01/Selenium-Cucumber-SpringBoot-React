package com.framework.apiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupRunFailures {
    private String runId;
    private LocalDateTime startTime;
    private String tags;
    private List<String> scenarios;
}
