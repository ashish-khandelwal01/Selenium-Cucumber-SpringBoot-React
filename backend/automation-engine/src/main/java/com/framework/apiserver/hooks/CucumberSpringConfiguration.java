package com.framework.apiserver.hooks;

import com.framework.apiserver.ApiServerApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = ApiServerApplication.class)
public class CucumberSpringConfiguration {
    // This class is only needed to initialize Spring context
    static {
        System.out.println("🔧 CucumberSpringConfiguration loaded");
    }
}