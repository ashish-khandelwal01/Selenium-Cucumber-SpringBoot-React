package com.framework.apiserver.controller;

import com.framework.apiserver.dto.FeatureUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/features")
@CrossOrigin(origins = "*")
public class FeatureController {

    private static final String FEATURE_PATH = "src/test/resources/features/";

    /**
     * Lists all feature files in the features directory.
     * @return list of feature file names
     */
    @Operation(
        summary = "List all feature files",
        description = "Lists all feature files in the features directory.",
        responses = {
            @ApiResponse(responseCode = "200", description = "List of feature files retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping
    public List<String> listFeatures() {
        return Arrays.stream(new File(FEATURE_PATH).listFiles())
                .filter(f -> f.getName().endsWith(".feature"))
                .map(File::getName)
                .toList();
    }

    /**
     * Reads the content of a specific feature file.
     * @param name the name of the feature file
     * @return the content of the feature file
     * @throws IOException if the file cannot be read
     */
    @Operation(
        summary = "Read feature file content",
        description = "Reads the content of a specific feature file.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Feature file content retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @GetMapping("/{name}")
    public String readFeature(@PathVariable String name) throws IOException {
        return Files.readString(Path.of(FEATURE_PATH + name));
    }

    /**
     * Updates the content of a specific feature file.
     * @param name the name of the feature file
     * @param content the new content for the feature file
     * @throws IOException if the file cannot be written
     */
    @Operation(
        summary = "Update feature file content",
        description = "Updates the content of a specific feature file.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Feature file updated successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
        }
    )
    @PutMapping("/{name}")
    public void updateFeature(@PathVariable String name, @RequestBody FeatureUpdateRequest content) throws IOException {
        Files.writeString(Path.of(FEATURE_PATH + name), content.getContent(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
