package com.yourssu.scouter.common.application.support.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.Scopes
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfiguration {

    @Value("\${domain}")
    private lateinit var domain: String

    companion object {
        const val OAUTH2 = "oauth2"
    }

    @Bean
    fun customOpenAPI() : OpenAPI {
        val securityRequirement = SecurityRequirement().addList(OAUTH2)
        val components = Components()
            .addSecuritySchemes(OAUTH2, createGoogleOAuth2Scheme())
        return OpenAPI()
            .components(Components())
            .info(info())
            .addSecurityItem(securityRequirement)
            .components(components)
    }

    private fun createGoogleOAuth2Scheme() : SecurityScheme {
        return SecurityScheme()
            .type(SecurityScheme.Type.OAUTH2)
            .flows(
                OAuthFlows()
                    .authorizationCode(
                        OAuthFlow()
                            .authorizationUrl("https://accounts.google.com/o/oauth2/auth")
                            .tokenUrl("${domain}/oauth2/swagger/callback")
                            .refreshUrl("${domain}/oauth2/swagger/callback")
                            .scopes(createGoogleScopes())
                    )
            )
    }

    private fun createGoogleScopes() : Scopes {
        return Scopes()
            .addString("openid", "OpenId Connect")
            .addString("profile", "기본 프로필 정보")
            .addString("email", "이메일 주소")
            .addString("https://www.googleapis.com/auth/drive.readonly", "드라이브 읽기 권한")
            .addString("https://www.googleapis.com/auth/forms.responses.readonly", "구글 폼 응답 읽기 권한")
            .addString("https://www.googleapis.com/auth/gmail.send", "메일 전송 권한")
    }

    private fun info() : Info {
        return Info()
            .title("Scouter API")
            .description("Scouter API")
            .version("v1.0.0")
    }
}