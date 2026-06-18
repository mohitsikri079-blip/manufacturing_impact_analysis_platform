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
        return new OpenAPI()
                .info(new Info()
                        .title("Manufacturing Impact Analysis Engine API")
                        .version("0.1.0")
                        .description("Ingests manufacturing ERP events into Neo4j and exposes deterministic impact analysis APIs."))
                .addSecurityItem(new SecurityRequirement().addList(apiKeyScheme))
                .schemaRequirement(apiKeyScheme, new SecurityScheme()
                        .name("X-API-Key")
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER));
    }
}
