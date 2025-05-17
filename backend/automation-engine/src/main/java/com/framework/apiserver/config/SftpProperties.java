package com.framework.apiserver.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SftpProperties is a configuration class that maps properties
 * prefixed with "sftp" from the application's configuration file.
 *
 * <p>It provides properties for managing SFTP server connection details,
 * such as the host, port, username, and password.</p>
 *
 * <p>Annotations:</p>
 * <ul>
 *   <li>@Component: Marks this class as a Spring-managed component.</li>
 *   <li>@ConfigurationProperties: Binds properties with the prefix "sftp".</li>
 *   <li>@Getter and @Setter: Lombok annotations to generate getter and setter methods.</li>
 * </ul>
 *
 * <p>Usage:</p>
 * <ul>
 *   <li>Define "sftp.host", "sftp.port", "sftp.username", and "sftp.password"
 *       in the application properties or YAML file.</li>
 *   <li>Inject this class into other Spring components to access these properties.</li>
 * </ul>
 */
@Component
@ConfigurationProperties(prefix = "sftp")
@Getter
@Setter
public class SftpProperties {

    /**
     * The hostname or IP address of the SFTP server.
     */
    private String host;

    /**
     * The port number of the SFTP server.
     */
    private int port;

    /**
     * The username for connecting to the SFTP server.
     */
    private String username;

    /**
     * The password for connecting to the SFTP server.
     */
    private String password;
}