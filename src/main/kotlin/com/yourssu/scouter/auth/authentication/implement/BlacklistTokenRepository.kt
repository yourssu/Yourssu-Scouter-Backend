package com.yourssu.scouter.auth.authentication.implement

interface BlacklistTokenRepository {

    fun saveAll(blacklistTokens: List<BlacklistToken>): List<BlacklistToken>
    fun existsByUserIdAndToken(userId: Long, targetToken: String): Boolean
}
