package com.yourssu.scouter.common.implement.domain.authentication

interface BlacklistTokenRepository {

    fun saveAll(blacklistTokens: List<BlacklistToken>): List<BlacklistToken>
    fun existsByUserIdAndToken(userId: Long, targetToken: String): Boolean
}
