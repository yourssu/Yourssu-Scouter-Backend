package com.yourssu.scouter.common.application.support.configuration

import com.yourssu.scouter.common.application.support.authentication.AuthUserInfoArgumentResolver
import com.yourssu.scouter.common.application.support.authentication.LoginInterceptor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableConfigurationProperties(CorsProperties::class)
@EnableScheduling
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
            .excludePathPatterns("/members/member-upload.html")
            .excludePathPatterns("/members/include-from-excel")
            .excludePathPatterns("/members/download-to-excel")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authUserInfoArgumentResolver)
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins(*corsProperties.allowedOrigins)
            .allowedHeaders("*")
            .allowedMethods(*HttpMethod.values().map { it.name() }.toTypedArray())
            .exposedHeaders(HttpHeaders.LOCATION)
            .allowCredentials(true)
    }
}
