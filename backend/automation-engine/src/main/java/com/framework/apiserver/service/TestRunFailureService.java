package com.framework.apiserver.service;

import com.framework.apiserver.dto.GroupRunFailures;
import com.framework.apiserver.dto.RunFailures;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface TestRunFailureService {

    Map<String, List<String>> getGroupedFailures();
    Page<GroupRunFailures> getPaginatedFailures(Pageable pageable);

}
