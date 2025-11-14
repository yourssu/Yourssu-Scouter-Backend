package com.yourssu.scouter.common.implement.support.configuration

import feign.Logger
import feign.codec.ErrorDecoder
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import com.yourssu.scouter.common.implement.support.security.oauth2.GoogleFeignErrorDecoder

@Configuration
@EnableFeignClients(basePackages = ["com.yourssu.scouter"])
class OpenFeignConfiguration {

    @Bean
    @Profile("local", "dev")
    fun feignLoggerLevel(): Logger.Level = Logger.Level.BASIC

    @Bean
    fun googleFeignErrorDecoder(): ErrorDecoder = GoogleFeignErrorDecoder()
}
