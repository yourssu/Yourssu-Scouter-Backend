package com.yourssu.scouter.common.implement.support.security.oauth2

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2.google")
data class GoogleOAuth2Properties(
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
    val scope: List<String>
)
