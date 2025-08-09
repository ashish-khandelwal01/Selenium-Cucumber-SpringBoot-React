package com.framework.apiserver.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class GroupRunFailures {
    private String runId;
    private List<String> scenarios;
}
