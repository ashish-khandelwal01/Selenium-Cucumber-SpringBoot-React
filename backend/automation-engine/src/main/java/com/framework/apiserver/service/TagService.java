package com.framework.apiserver.service;

import com.framework.apiserver.entity.Tag;

import java.util.List;

/**
 * Service interface for managing tag-related operations.
 */
public interface TagService {

    /**
     * Extracts tags from feature files, saves new tags to the database, and retrieves all tags.
     *
     * @return A list of all Tag entities from the database.
     */
    List<Tag> extractSaveAndGetAllTags();
}