package com.yourssu.scouter.common.implement.domain.authentication

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class BlacklistTokenWriter(
    private val blacklistTokenRepository: BlacklistTokenRepository,
) {

    fun write(userId: Long, accessToken: String, refreshToken: String) {
        val blacklistTokens: MutableList<BlacklistToken> = mutableListOf()
        blacklistTokens.add(
            BlacklistToken(
                userId = userId,
                tokenType = TokenType.ACCESS,
                token = accessToken
            )
        )
        blacklistTokens.add(
            BlacklistToken(
                userId = userId,
                tokenType = TokenType.REFRESH,
                token = refreshToken
            )
        )

        blacklistTokenRepository.saveAll(blacklistTokens)
    }
}
