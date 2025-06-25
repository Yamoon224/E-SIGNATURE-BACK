package com.signature.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("assets");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/assets/**")
                .allowedOrigins("https://e-signature-seven.vercel.app")
                .allowedMethods("GET")
                .allowedHeaders("*")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
