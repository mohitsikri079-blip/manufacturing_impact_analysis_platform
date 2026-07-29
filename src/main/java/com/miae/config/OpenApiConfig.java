package com.miae.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for setting up OpenAPI documentation for the Manufacturing Impact Analysis Engine API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI miaeOpenApi() {
        String apiKeyScheme = "ApiKeyAuth";
        String bearerScheme = "BearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Manufacturing Impact Analysis Engine API")
                        .version("0.1.0")
                        .description("Ingests manufacturing ERP events into Neo4j and exposes deterministic impact analysis APIs. "
                                + "Authentication is selected by MIAE configuration: use the API key scheme for DEVELOPER_API_KEY, "
                                + "or Bearer JWT for JWT_PUBLIC_KEY and JWKS deployments."))
                .addSecurityItem(new SecurityRequirement().addList(apiKeyScheme))
                .addSecurityItem(new SecurityRequirement().addList(bearerScheme))
                .schemaRequirement(apiKeyScheme, new SecurityScheme()
                        .name("X-API-Key")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .description("Use when miae.security.authentication-type is DEVELOPER_API_KEY."))
                .schemaRequirement(bearerScheme, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Use when miae.security.authentication-type is JWT_PUBLIC_KEY or JWKS."));
    }
}
