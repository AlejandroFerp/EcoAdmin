package com.iesdoctorbalmis.spring.config;

import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sirve los archivos subidos a /uploads/documentos/* desde el directorio externo
 * configurado en application.properties (`ecoadmin.uploads.documentos`).
 */
@Configuration
public class UploadsWebConfig implements WebMvcConfigurer {

    @Value("${ecoadmin.uploads.documentos:uploads/documentos}")
    private String directorioUploads;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluto = Paths.get(directorioUploads).toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/documentos/**")
                .addResourceLocations(absoluto);
    }
}
