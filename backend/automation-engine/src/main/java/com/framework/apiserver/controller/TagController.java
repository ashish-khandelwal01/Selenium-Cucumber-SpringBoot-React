package com.framework.apiserver.controller;

import com.framework.apiserver.entity.Tag;
import com.framework.apiserver.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for handling requests related to tags.
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

    private final TagService tagScannerService;

    /**
     * Constructor for TagController.
     *
     * @param tagScannerService The service used for tag-related operations.
     */
    public TagController(TagService tagScannerService) {
        this.tagScannerService = tagScannerService;
    }

    /**
     * Retrieves all tags by extracting, saving, and returning them.
     *
     * @return A list of Tag objects.
     */
    @Operation(
            summary = "Get all tags",
            description = "Extracts, saves, and returns all tags.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of all tags retrieved successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    @GetMapping
    public List<Tag> getTags() {
        return tagScannerService.extractSaveAndGetAllTags();
    }
}