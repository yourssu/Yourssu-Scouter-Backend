package com.yourssu.scouter.common.storage.domain.authentication

import org.springframework.data.jpa.repository.JpaRepository

interface JpaBlacklistTokenRepository : JpaRepository<BlacklistTokenEntity, Long> {

    fun existsByUserIdAndToken(userId: Long, token: String): Boolean
}
