package com.framework.apiserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class for backend-specific settings.
 * Defines beans and configurations for the Spring application context.
 */
@Configuration
public class BackendConfig {

    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings for the application.
     *
     * @return A WebMvcConfigurer instance with customized CORS mappings.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Adds CORS mappings to allow cross-origin requests.
             *
             * @param registry The CorsRegistry to configure CORS mappings.
             */
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Allow all endpoints.
                        .allowedOrigins("http://localhost:3000") // Allow requests from the specified origin.
                        .allowedMethods("*"); // Allow all HTTP methods (GET, POST, PUT, DELETE, etc.).
            }
        };
    }
}