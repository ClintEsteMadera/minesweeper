package com.jchiocchio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures several aspects of Spring MVC
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configures cross origin requests processing.
     */
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedMethods("GET", "HEAD", "POST", "PATCH");
    }
}
