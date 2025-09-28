package com.framework.apiserver.dto;

@lombok.Data
@lombok.Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class ExecutionStats {
    private boolean kafkaEnabled;
    private boolean kafkaAvailable;
    private boolean fallbackEnabled;
}
