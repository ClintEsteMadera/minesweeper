package com.jchiocchio.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import lombok.SneakyThrows;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
public class OpenApiConfig {

    @Value("classpath:openapi-description.md")
    private Resource openAPIDescriptionFilePath;

    @Value("${server.ssl.enabled:false}")
    private boolean sslEnabled;

    @Value("${server.port}")
    private String port;

    @Autowired
    private ServletContext servletContext;

    @Bean
    @SneakyThrows
    public String serverBaseUrl() {
        var scheme = sslEnabled ? "https" : "http";
        var hostname = InetAddress.getLocalHost().getHostAddress();
        var contextPath = servletContext.getContextPath();

        return scheme + "://" + hostname + ":" + port + contextPath;
    }

    @Bean
    public String openAPIDescription() {
        try (InputStream is = openAPIDescriptionFilePath.getInputStream()) {
            return StreamUtils.copyToString(is, UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read Open API's description from " + openAPIDescriptionFilePath.getFilename());
        }
    }

    @Bean
    public OpenAPI openAPI(String openAPIDescription, String serverBaseUrl) {
        return new OpenAPI()
            .info(new Info()
                      .title("Minesweeper 2020")
                      .description(openAPIDescription)
                      .version("0.0.1"));
    }
}
