package com.framework.apiserver.service;

import com.framework.apiserver.dto.ReportMetaData;
import com.framework.apiserver.entity.TestRunInfoEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ReportService {

    ResponseEntity<?> getReportZip(String runId);
    List<TestRunInfoEntity> listAllReports();
    ResponseEntity<?> viewSparkReport(String runId);
}
