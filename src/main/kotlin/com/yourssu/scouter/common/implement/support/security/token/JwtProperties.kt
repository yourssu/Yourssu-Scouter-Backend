package com.yourssu.scouter.common.implement.support.security.token

import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "token.jwt")
data class JwtProperties(
    private val accessKey: String,
    private val refreshKey: String,
    private val accessExpiredHours: Long,
    private val refreshExpiredHours: Long
) {
    fun findTokenKey(tokenType: TokenType): String {
        if (TokenType.ACCESS === tokenType) {
            return accessKey
        }

        return refreshKey
    }

    fun findExpiredHours(tokenType: TokenType): Long {
        if (TokenType.ACCESS === tokenType) {
            return accessExpiredHours
        }

        return refreshExpiredHours
    }
}
