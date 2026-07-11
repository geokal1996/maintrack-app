package com.codingfactory.maintrack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Auto einai gia na exei omorfo titlo/perigrafi to Swagger UI - proairetiko, alla oreo.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI maintrackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maintrack API")
                        .description("Sistima katagrafis vlavon kai syntirisis - Coding Factory Final Project")
                        .version("v1"));
    }
}
