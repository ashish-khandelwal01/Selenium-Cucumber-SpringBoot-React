package com.framework.apiserver.config;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utility class for accessing the Spring application context.
 * Implements ApplicationContextAware to set the application context statically,
 * allowing beans to be retrieved programmatically.
 */
@Component
public class SpringContext implements ApplicationContextAware {

    /**
     * Static reference to the Spring application context.
     */
    private static ApplicationContext context;

    /**
     * Sets the application context. This method is called by the Spring framework
     * during application initialization.
     *
     * @param applicationContext The application context to set.
     * @throws BeansException if the application context cannot be set.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.context = applicationContext;
    }

    /**
     * Retrieves a bean from the Spring application context by its class type.
     *
     * @param <T>   The type of the bean to retrieve.
     * @param clazz The class type of the bean.
     * @return The bean instance of the specified type.
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }
}