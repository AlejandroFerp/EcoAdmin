package com.alejandrofernandez.ecoadmin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI ecoadminOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("EcoAdmin API")
                .description("API REST para la gestión de traslados de residuos peligrosos (baterías de litio). "
                    + "Permite consultar y gestionar centros, residuos, traslados y su historial de estados.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("IES Doctor Balmis")
                    .email("admin@ecoadmin.com")));
    }
}
