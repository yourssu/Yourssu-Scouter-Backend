package com.yourssu.scouter.common.implement.support.configuration

import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(basePackages = ["com.yourssu.scouter"])
class OpenFeignConfiguration {
}
