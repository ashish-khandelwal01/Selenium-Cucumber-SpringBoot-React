package com.framework.apiserver.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * MySqlDataSourceConfig is a configuration class for setting up the MySQL DataSource
 * and JdbcTemplate beans used in the application.
 *
 * <p>Dependencies:</p>
 * <ul>
 *   <li>Spring Framework for configuration and bean management</li>
 *   <li>HikariCP for connection pooling</li>
 *   <li>MySqlDbProperties for database connection properties</li>
 * </ul>
 *
 * <p>Beans:</p>
 * <ul>
 *   <li>mysqlDataSource: Configures and provides a HikariDataSource for MySQL</li>
 *   <li>mysqlJdbcTemplate: Configures and provides a JdbcTemplate for MySQL</li>
 * </ul>
 *
 * @see DataSource
 * @see JdbcTemplate
 * @see HikariDataSource
 */
@Configuration
public class MySqlDataSourceConfig {

    private final MySqlDbProperties properties;

    /**
     * Constructs a MySqlDataSourceConfig instance with the required MySqlDbProperties dependency.
     *
     * @param properties The MySqlDbProperties instance containing database connection details.
     */
    public MySqlDataSourceConfig(MySqlDbProperties properties) {
        this.properties = properties;
    }

    /**
     * Configures and provides a HikariDataSource bean for MySQL.
     *
     * @return The configured DataSource instance.
     */
    @Bean(name = "mysqlDataSource")
    public DataSource mysqlDataSource() {
        HikariDataSource dataSource = DataSourceBuilder.create()
                .driverClassName(properties.getDriverClassName())
                .url(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .type(HikariDataSource.class)
                .build();
        return dataSource;
    }

    /**
     * Configures and provides a JdbcTemplate bean for MySQL.
     *
     * @param dataSource The DataSource instance to be used by the JdbcTemplate.
     * @return The configured JdbcTemplate instance.
     */
    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}