package com.example.gestionvacantes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuración para habilitar el envío asíncrono de emails
 */
@Configuration
@EnableAsync
public class EmailConfig {
    // La configuración real está en application.properties
    // Esta clase solo habilita @Async
}