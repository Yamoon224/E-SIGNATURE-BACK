package com.signature.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Configuration CORS pour autoriser Vercel à accéder aux fichiers statiques
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/assets/**")
                .allowedOrigins("https://e-signature-seven.vercel.app")
                .allowedMethods("GET")
                .allowedHeaders("*");
    }

    // Configuration pour servir le dossier /assets en tant que ressources statiques
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        Path uploadDir = Paths.get("assets");
        String uploadPath = uploadDir.toFile().getAbsolutePath();

        registry.addResourceHandler("/assets/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
}
