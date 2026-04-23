package com.ecommerce.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eCommerceOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce Analytics Platform API") // Matches your project title [cite: 301]
                        .version("1.0")
                        .description("API documentation for the CSE 214 Multi-Agent Text2SQL E-Commerce backend.")) // [cite: 2, 301]
                
                // 1. Require the security scheme globally for all endpoints
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                
                // 2. Define the JWT Bearer security scheme
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT"))); // Matches the JWT requirement [cite: 357]
    }
}