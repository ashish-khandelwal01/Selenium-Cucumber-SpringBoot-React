package com.framework.apiserver.service;

import com.framework.apiserver.entity.TestRunInfoEntity;
import com.framework.apiserver.repository.TestRunInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TestRunInfoService {

    private final TestRunInfoRepository repository;

    public void save(TestRunInfoEntity entity) {
        repository.save(entity);
    }
}
