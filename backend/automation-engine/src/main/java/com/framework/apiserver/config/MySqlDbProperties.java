package com.framework.apiserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * MySqlDbProperties is a configuration class that maps properties
 * prefixed with "mysql.datasource" from the application's configuration file.
 *
 * <p>It provides properties for managing MySQL database connection details,
 * such as the URL, username, password, and driver class name.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Configuration: Marks this class as a Spring configuration component.</li>
 *   <li>@ConfigurationProperties: Binds properties with the prefix "mysql.datasource".</li>
 *   <li>@Getter and @Setter: Lombok annotations to generate getter and setter methods.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Define "mysql.datasource.url", "mysql.datasource.username", "mysql.datasource.password",
 *       and "mysql.datasource.driverClassName" in the application properties or YAML file.</li>
 *   <li>Inject this class into other Spring components to access these properties.</li>
 * </ul>
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "mysql.datasource")
public class MySqlDbProperties {

    /**
     * The URL of the MySQL database.
     */
    private String url;

    /**
     * The username for connecting to the MySQL database.
     */
    private String username;

    /**
     * The password for connecting to the MySQL database.
     */
    private String password;

    /**
     * The fully qualified name of the MySQL JDBC driver class.
     */
    private String driverClassName;
}