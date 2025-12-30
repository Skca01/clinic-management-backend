package com.amante.clinicmanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Medicare API",
                version = "1.0",
                description = "Clinic Management System API\n\n" +
                        "**Frontend Website:** [medicare-beta-orpin.vercel.app](https://medicare-beta-orpin.vercel.app/)\n\n" +
                        "### [Code Quality Dashboard](https://sonarcloud.io/project/overview?id=Skca01_clinic-management-backend)\n" +
                        "![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=alert_status) " +
                        "![Security](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=security_rating) " +
                        "![Reliability](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=reliability_rating) " +
                        "![Maintainability](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=sqale_rating)\n\n" +
                        "![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=vulnerabilities) " +
                        "![Bugs](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=bugs) " +
                        "![Coverage](https://sonarcloud.io/api/project_badges/measure?project=Skca01_clinic-management-backend&metric=coverage)",
                contact = @Contact(
                        name = "Kent Carlo B. Amante",
                        email = "carloamante125@gmail.com"
                )
        ),
        servers = {
                @Server(
                        description = "Production (Render)",
                        url = "https://clinic-management-backend-x4y8.onrender.com"
                ),
                @Server(
                        description = "Local Development",
                        url = "http://localhost:8080"
                )
        },
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        description = "JWT token authentication. Add 'Bearer ' prefix before your token.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}