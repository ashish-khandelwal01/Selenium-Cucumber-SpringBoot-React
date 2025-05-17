package com.framework.apiserver.dto;

import lombok.Getter;

@Getter
public class TestExecutionResponse {
    private String status;
    private int exitCode;

    public TestExecutionResponse(String status, int exitCode) {
        this.status = status;
        this.exitCode = exitCode;
    }

}
