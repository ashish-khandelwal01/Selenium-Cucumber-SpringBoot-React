package com.framework.apiserver.controller;

import com.framework.apiserver.dto.FeatureUpdateRequest;
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

    @GetMapping
    public List<String> listFeatures() {
        return Arrays.stream(new File(FEATURE_PATH).listFiles())
                .filter(f -> f.getName().endsWith(".feature"))
                .map(File::getName)
                .toList();
    }

    @GetMapping("/{name}")
    public String readFeature(@PathVariable String name) throws IOException {
        return Files.readString(Path.of(FEATURE_PATH + name));
    }

    @PutMapping("/{name}")
    public void updateFeature(@PathVariable String name, @RequestBody FeatureUpdateRequest content) throws IOException {
        Files.writeString(Path.of(FEATURE_PATH + name), content.getContent(),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
