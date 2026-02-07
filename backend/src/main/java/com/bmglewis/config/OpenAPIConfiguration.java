package com.bmglewis.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * FILE TYPE: CLASS (@Configuration)
 * OpenAPI/Swagger configuration
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Procurement App API",
                version = "1.0",
                description = "Simple JWT Authentication for Procurement Application",
                contact = @Contact(
                        name = "Procurement Team",
                        email = "support@bmglewis.com"
                )
        ),
//        servers = {
//                @Server(url = "http://localhost:8080/api", description = "Local Server")
//        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Authentication",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenAPIConfiguration {
}