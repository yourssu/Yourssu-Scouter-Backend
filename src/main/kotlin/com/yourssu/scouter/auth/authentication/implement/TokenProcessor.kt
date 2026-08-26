package com.yourssu.scouter.auth.authentication.implement

import io.jsonwebtoken.Claims
import java.time.Instant

interface TokenProcessor {

    fun encode(
        issueTime: Instant,
        tokenType: TokenType,
        privateClaims: Map<String, Any>
    ): String

    fun decode(tokenType: TokenType, token: String): Claims?

    fun generateToken(issueTime: Instant, privateClaims: Map<String, Any>): Token
}
