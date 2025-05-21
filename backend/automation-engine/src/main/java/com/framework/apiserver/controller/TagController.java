package com.framework.apiserver.controller;

import com.framework.apiserver.entity.Tag;
import com.framework.apiserver.service.TagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagScannerService;

    public TagController(TagService tagScannerService) {
        this.tagScannerService = tagScannerService;
    }

    @GetMapping
    public List<Tag> getTags() {
        return tagScannerService.extractSaveAndGetAllTags();
    }
}
