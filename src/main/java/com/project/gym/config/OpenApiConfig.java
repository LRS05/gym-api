package com.project.gym.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig
{
    private static final String SECURITY_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI openAPI()
    {
        return new OpenAPI()
                .info(new Info()
                        .title("API GYM")
                        .version("1.0.0")
                        .description("API for Client and Memberships Management of GYM")
                        .contact(new Contact()
                                .name("Lorenzo Sarlo")
                                .email("lorenzosarlo73@gmail.com")
                        )
                )
                .servers(List.of(
                                new Server().url("http://localhost:8080").description("DEV Server")
                        )
                )
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Authorization Header with JWT Token")
                        )
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME)
                );
    }
}
