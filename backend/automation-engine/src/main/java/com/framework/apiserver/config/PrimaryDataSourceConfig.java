package com.framework.apiserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import javax.sql.DataSource;

/**
 * Configuration class for setting up the primary data source.
 * This configuration is activated only when the "postgres" profile is active.
 */
@Configuration
@Profile("postgres")
public class PrimaryDataSourceConfig {

    /**
     * Defines the primary data source bean for the application.
     * The data source properties are loaded from the configuration file
     * using the prefix "spring.datasource".
     *
     * @return A DataSource instance configured with the specified properties.
     */
    @Primary
    @Bean(name = "primaryDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

}