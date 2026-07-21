package com.yourssu.scouter.auth.authentication.implement

class BlacklistToken(
    val id: Long? = null,
    val userId: Long,
    val tokenType: TokenType,
    val token: String,
)
