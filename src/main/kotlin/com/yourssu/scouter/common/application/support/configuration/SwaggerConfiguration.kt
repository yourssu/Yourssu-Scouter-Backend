package com.yourssu.scouter.common.application.support.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {

    companion object {
        const val JWT = "JWT"
        const val BEARER = "Bearer"
    }

    @Bean
    fun customOpenAPI() : OpenAPI {
        val securityRequirement = SecurityRequirement().addList(JWT)
        val components = Components().addSecuritySchemes(
            JWT, SecurityScheme()
                .name(JWT)
                .type(SecurityScheme.Type.HTTP)
                .scheme(BEARER)
                .bearerFormat(JWT)
        )
        return OpenAPI()
            .components(Components())
            .info(info())
            .addSecurityItem(securityRequirement)
            .components(components)
    }

    private fun info() : Info {
        return Info()
            .title("Scouter API")
            .description("Scouter API")
            .version("v1.0.0")
    }
}