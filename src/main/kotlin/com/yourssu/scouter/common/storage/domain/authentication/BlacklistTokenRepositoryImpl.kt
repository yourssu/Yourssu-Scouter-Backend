package com.yourssu.scouter.common.storage.domain.authentication

import com.yourssu.scouter.common.implement.domain.authentication.BlacklistToken
import com.yourssu.scouter.common.implement.domain.authentication.BlacklistTokenRepository
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
