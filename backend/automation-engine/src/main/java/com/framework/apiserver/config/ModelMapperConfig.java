package com.framework.apiserver.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up the ModelMapper bean.
 * This class provides a centralised configuration for the ModelMapper library,
 * which is used for object mapping in the application.
 */
@Configuration
public class ModelMapperConfig {

    /**
     * Creates and configures a ModelMapper bean.
     * The ModelMapper is used to map objects from one type to another,
     * simplifying the conversion between DTOs and entities.
     *
     * @return A new instance of the ModelMapper.
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }
}