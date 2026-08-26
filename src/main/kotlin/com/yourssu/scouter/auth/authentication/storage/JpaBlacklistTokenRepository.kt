package com.yourssu.scouter.auth.authentication.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaBlacklistTokenRepository : JpaRepository<BlacklistTokenEntity, Long> {

    fun existsByUserIdAndToken(userId: Long, token: String): Boolean
}
