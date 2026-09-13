package com.SmartIndiaHackathon.kishan_suvidha_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Kisan Suvidha API",
                version = "1.0.0",
                description = """
                        REST API for Kisan Suvidha.

                        Kisan Suvidha provides digital workflows for:
                        - Farmer authentication and profiles
                        - Crop registration
                        - Procurement centre discovery
                        - Procurement queues
                        - Marketplace offers
                        - Transactions
                        - Transport and logistics
                        """
        ),
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {
}
