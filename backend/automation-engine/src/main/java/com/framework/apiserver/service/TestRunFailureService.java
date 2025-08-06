package com.framework.apiserver.service;

import java.util.List;
import java.util.Map;

public interface TestRunFailureService {

    Map<String, List<String>> getGroupedFailures();
}
