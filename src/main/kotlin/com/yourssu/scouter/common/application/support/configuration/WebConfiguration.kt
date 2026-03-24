package com.yourssu.scouter.common.application.support.configuration

import com.yourssu.scouter.common.application.support.authentication.AuthUserInfoArgumentResolver
import com.yourssu.scouter.common.application.support.authentication.LoginInterceptor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
class WebConfiguration(
    private val loginInterceptor: LoginInterceptor,
    private val authUserInfoArgumentResolver: AuthUserInfoArgumentResolver,
    private val corsProperties: CorsProperties,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/favicon.ico")
            .excludePathPatterns("/error")
            .excludePathPatterns("/oauth2/**")
            .excludePathPatterns("/validate-token")
            .excludePathPatterns("/refresh-token")
            .excludePathPatterns("/members/upload")
            .excludePathPatterns("/members/upload/auth")
            .excludePathPatterns("/members/member-upload.html")
            .excludePathPatterns("/members/member-upload-password.html")
            .excludePathPatterns("/members/include-from-excel")
            .excludePathPatterns("/members/download-to-excel")
            .excludePathPatterns("/api/mails/images/**")
            .excludePathPatterns("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/swagger-resources/**")
            .excludePathPatterns("/actuator/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authUserInfoArgumentResolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOriginPatterns(*corsProperties.allowedOriginPatterns)
            .allowedHeaders("*")
            .allowedMethods(*HttpMethod.values().map { it.name() }.toTypedArray())
            .exposedHeaders(HttpHeaders.LOCATION)
            .allowCredentials(true)
    }
}
