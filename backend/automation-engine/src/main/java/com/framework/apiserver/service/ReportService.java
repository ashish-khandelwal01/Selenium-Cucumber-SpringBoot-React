package com.framework.apiserver.service;

import com.framework.apiserver.dto.ReportMetaData;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReportService {

    ResponseEntity<?> getReportZip(String runId);
    List<ReportMetaData> listAllReports();
    ResponseEntity<?> viewSparkReport(String runId);
}
