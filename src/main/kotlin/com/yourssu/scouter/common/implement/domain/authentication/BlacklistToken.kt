package com.yourssu.scouter.common.implement.domain.authentication

class BlacklistToken(
    val id: Long? = null,
    val userId: Long,
    val tokenType: TokenType,
    val token: String,
)
