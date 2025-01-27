package com.yourssu.scouter.common.implement.support.security.token

import java.time.LocalDateTime

interface TokenProcessor {

    fun encode(
        issueTime: LocalDateTime,
        tokenType: TokenType,
        privateClaims: Map<String, Any>
    ): String
}
