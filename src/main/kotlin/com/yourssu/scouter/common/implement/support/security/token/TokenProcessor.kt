package com.yourssu.scouter.common.implement.support.security.token

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
