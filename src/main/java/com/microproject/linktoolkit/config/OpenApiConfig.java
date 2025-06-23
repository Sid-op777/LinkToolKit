package com.microproject.linktoolkit.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${app.base-url}")
    private String baseUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        // Define the JWT Bearer security scheme
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .servers(List.of(new Server().url(baseUrl))) // Sets the server URL for the API
                .info(new Info()
                        .title("LinkToolkit API")
                        .version("v1.0")
                        .description("This is the official REST API for the LinkToolkit URL shortening service.")
                        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT"))
                )
                // Add security definitions to the components
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                )
                // Apply the security requirement globally to all endpoints
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}