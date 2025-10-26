package com.example.gestionvacantes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración para servir archivos estáticos (CVs)
 */
@Configuration
public class FileStorageConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Sirve los archivos de uploads/cvs en la URL /uploads/cvs/**
        registry.addResourceHandler("/uploads/cvs/**")
                .addResourceLocations("file:uploads/cvs/");
    }
}