package com.yourssu.scouter.common.implement.domain.authentication

import io.jsonwebtoken.Claims
import java.time.LocalDateTime

interface TokenProcessor {

    fun encode(
        issueTime: LocalDateTime,
        tokenType: TokenType,
        privateClaims: Map<String, Any>
    ): String

    fun decode(tokenType: TokenType, token: String): Claims?
}
