package com.codingfactory.maintrack.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Auto einai gia na exei omorfo titlo/perigrafi to Swagger UI, KAI gia na emfanistei
// to koumpi "Authorize" ώστε να mporoume na dokimazoume protected endpoints apo to UI.
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI maintrackOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maintrack API")
                        .description("Sistima katagrafis vlavon kai syntirisis - Coding Factory Final Project")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
