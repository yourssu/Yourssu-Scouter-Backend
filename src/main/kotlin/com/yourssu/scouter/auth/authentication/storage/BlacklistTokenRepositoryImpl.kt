package com.yourssu.scouter.auth.authentication.storage

import com.yourssu.scouter.auth.authentication.implement.BlacklistToken
import com.yourssu.scouter.auth.authentication.implement.BlacklistTokenRepository
import org.springframework.stereotype.Repository

@Repository
class BlacklistTokenRepositoryImpl(
    private val jpaBlacklistTokenRepository: JpaBlacklistTokenRepository,
) : BlacklistTokenRepository {

    override fun saveAll(blacklistTokens: List<BlacklistToken>): List<BlacklistToken> {
        return jpaBlacklistTokenRepository.saveAll(
            blacklistTokens.map { BlacklistTokenEntity.from(it) }
        ).map { it.toDomain() }
    }

    override fun existsByUserIdAndToken(userId: Long, targetToken: String): Boolean {
        return jpaBlacklistTokenRepository.existsByUserIdAndToken(userId, targetToken)
    }
}
