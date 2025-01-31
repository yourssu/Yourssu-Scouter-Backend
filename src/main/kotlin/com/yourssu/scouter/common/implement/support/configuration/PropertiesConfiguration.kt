package com.yourssu.scouter.common.implement.support.configuration

import com.yourssu.scouter.common.implement.support.security.oauth2.GoogleOAuth2Properties
import com.yourssu.scouter.common.implement.support.security.token.JwtProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(JwtProperties::class, GoogleOAuth2Properties::class)
class PropertiesConfiguration
