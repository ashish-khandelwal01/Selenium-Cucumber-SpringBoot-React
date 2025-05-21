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

@Service
public class TagServiceImpl implements TagService {

    private final BaseClass baseClass;
    private final TagRepository tagRepository;

    public TagServiceImpl(BaseClass baseClass, TagRepository tagRepository) {
        this.baseClass = baseClass;
        this.tagRepository = tagRepository;
    }

    public List<Tag> extractSaveAndGetAllTags() {
        try {
            extractTags();
        } catch (IOException e) {
            baseClass.failLog("Error extracting tags: " + e.getMessage());
        }
        return tagRepository.findAll();
    }

    public void extractTags() throws IOException {
        Path featuresDir = Paths.get("src/test/resources/features");
        Set<String> foundTags = new HashSet<>();

        Files.walk(featuresDir)
                .filter(p -> p.toString().endsWith(".feature"))
                .forEach(file -> {
                    try {
                        List<String> lines = Files.readAllLines(file);
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

        List<String> existingTags = tagRepository.findAll()
                .stream()
                .map(Tag::getName)
                .collect(Collectors.toList());

        foundTags.removeAll(existingTags);

        List<Tag> newTags = foundTags.stream().map(Tag::new).toList();
        tagRepository.saveAll(newTags);
    }
}
