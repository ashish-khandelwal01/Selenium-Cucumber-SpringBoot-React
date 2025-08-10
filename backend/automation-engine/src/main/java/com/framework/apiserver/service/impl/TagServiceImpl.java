package com.framework.apiserver.service.impl;

import com.framework.apiserver.entity.Tag;
import com.framework.apiserver.repository.TagRepository;
import com.framework.apiserver.service.TagService;
import com.framework.apiserver.utilities.BaseClass;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service implementation for handling tag-related operations.
 */
@Service
public class TagServiceImpl implements TagService {

    private final BaseClass baseClass;
    private final TagRepository tagRepository;

    /**
     * Constructor for TagServiceImpl.
     *
     * @param baseClass The utility class for logging and other base operations.
     * @param tagRepository The repository for performing CRUD operations on Tag entities.
     */
    public TagServiceImpl(BaseClass baseClass, TagRepository tagRepository) {
        this.baseClass = baseClass;
        this.tagRepository = tagRepository;
    }

    /**
     * Extracts tags from feature files, saves new tags to the database, and retrieves all tags.
     *
     * @return A list of all Tag entities from the database.
     */
    public List<Tag> extractSaveAndGetAllTags() {
        try {
            extractTags();
        } catch (IOException e) {
            baseClass.failLog("Error extracting tags: " + e.getMessage());
        }
        return tagRepository.findAll();
    }

    /**
     * Extracts tags from feature files located in the `src/test/resources/features` directory.
     * New tags are saved to the database, while existing tags are ignored.
     *
     * @throws IOException If an error occurs while reading the feature files.
     */
    public void extractTags() throws IOException {
        Path featuresDir = Paths.get("src/test/resources/features");
        Set<String> foundTags = new HashSet<>();

        // Walk through all files in the features directory and process `.feature` files
        Files.walk(featuresDir)
                .filter(p -> p.toString().endsWith(".feature"))
                .forEach(file -> {
                    try {
                        List<String> lines = Files.readAllLines(file);
                        // Extract lines starting with '@' and collect tags
                        lines.stream()
                                .filter(line -> line.trim().startsWith("@"))
                                .forEach(line -> {
                                    String[] tags = line.trim().split("\\s+");
                                    foundTags.addAll(Arrays.asList(tags));
                                });
                    } catch (IOException e) {
                        throw new RuntimeException("Error reading feature file: " + file, e);
                    }
                });

        // Retrieve existing tags from the database
        List<String> existingTags = tagRepository.findAll()
                .stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        // Remove existing tags from the found tags
        foundTags.removeAll(existingTags);

        // Create new Tag entities for the remaining tags and save them to the database
        List<Tag> newTags = foundTags.stream().map(Tag::new).toList();
        tagRepository.saveAll(newTags);
    }
}