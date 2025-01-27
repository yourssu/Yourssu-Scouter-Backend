package com.yourssu.scouter.common.implement.support.security.token

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "token.jwt")
data class JwtProperties(
    private val accessKey: String,
    private val refreshKey: String,
    private val accessExpiredHours: Long,
    private val refreshExpiredHours: Long
) {
}
