package com.yourssu.scouter.common.implement.support.configuration

import feign.Logger
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@EnableFeignClients(basePackages = ["com.yourssu.scouter"])
class OpenFeignConfiguration {

    @Bean
    @Profile("local", "dev")
    fun feignLoggerLevel(): Logger.Level = Logger.Level.FULL
}
