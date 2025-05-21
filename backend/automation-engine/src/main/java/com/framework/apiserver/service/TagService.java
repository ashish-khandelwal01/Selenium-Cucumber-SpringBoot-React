package com.framework.apiserver.service;

import com.framework.apiserver.entity.Tag;

import java.util.List;

public interface TagService {
    List<Tag> extractSaveAndGetAllTags();
}
