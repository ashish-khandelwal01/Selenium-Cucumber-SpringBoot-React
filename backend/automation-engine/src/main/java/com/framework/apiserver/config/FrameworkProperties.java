package com.framework.apiserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * FrameworkProperties is a configuration class that maps properties
 * prefixed with "framework" from the application's configuration file.
 *
 * <p>It provides properties for managing file paths used in the framework,
 * such as download and screenshot paths.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Configuration: Marks this class as a Spring configuration component.</li>
 *   <li>@ConfigurationProperties: Binds properties with the prefix "framework".</li>
 *   <li>@Getter and @Setter: Lombok annotations to generate getter and setter methods.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Define "framework.downloadPath" and "framework.screenshotPath" in the application properties or YAML file.</li>
 *   <li>Inject this class into other Spring components to access these properties.</li>
 * </ul>
 */
@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "framework")
public class FrameworkProperties {

    /**
     * The path where files will be downloaded.
     */
    private String downloadPath;

    /**
     * The path where screenshots will be saved.
     */
    private String screenshotPath;

}