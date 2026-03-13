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
                        new Server().url("https://gym-api-4sn7.onrender.com").description("PROD Server")
                ))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("access-token")
                                        .description("JWT automatically sent by the browser through an HttpOnly cookie")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
    }
}

