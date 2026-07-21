package com.yourssu.scouter.auth.authentication.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class BlacklistTokenReader(
    private val blacklistTokenRepository: BlacklistTokenRepository,
) {

    fun isBlacklisted(userId: Long, targetToken: String): Boolean {
        return blacklistTokenRepository.existsByUserIdAndToken(userId, targetToken)
    }
}
