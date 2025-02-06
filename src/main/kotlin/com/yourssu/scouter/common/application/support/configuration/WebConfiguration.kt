package com.yourssu.scouter.common.application.support.configuration

import com.yourssu.scouter.common.application.support.authentication.AuthUserInfoArgumentResolver
import com.yourssu.scouter.common.application.support.authentication.LoginInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfiguration(
    private val loginInterceptor: LoginInterceptor,
    private val authUserInfoArgumentResolver: AuthUserInfoArgumentResolver
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(loginInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns("/oauth2/**")
            .excludePathPatterns("/validate-token")
            .excludePathPatterns("/refresh-token")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(authUserInfoArgumentResolver)
    }
}
