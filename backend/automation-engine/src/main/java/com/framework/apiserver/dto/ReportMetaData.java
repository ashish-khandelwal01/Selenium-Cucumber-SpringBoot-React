package com.framework.apiserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReportMetaData {

    @JsonProperty("Passed")
    private int passed;

    @JsonProperty("Failed")
    private int failed;

    @JsonProperty("Total")
    private int total;

    @JsonProperty("Duration in Seconds")
    private int durationInSeconds;

    @JsonProperty("runId")
    private String runId;

    @JsonProperty("Start Time")
    private String startTime;

    @JsonProperty("End Time")
    private String endTime;

    @JsonProperty("tags")
    private String tags;

    @JsonProperty("status")
    private String status;

}
