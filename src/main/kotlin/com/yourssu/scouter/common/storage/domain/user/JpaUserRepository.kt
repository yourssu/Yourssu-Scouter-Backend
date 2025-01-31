package com.yourssu.scouter.common.storage.domain.user

import org.springframework.data.jpa.repository.JpaRepository

interface JpaUserRepository : JpaRepository<UserEntity, Long> {

    fun findByOauthId(oauthId: String): UserEntity?
}
