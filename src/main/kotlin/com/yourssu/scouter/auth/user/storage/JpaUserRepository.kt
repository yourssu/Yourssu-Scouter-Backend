package com.yourssu.scouter.auth.user.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaUserRepository : JpaRepository<UserEntity, Long> {

    fun findByOauthId(oauthId: String): UserEntity?
    fun findByEmail(email: String): UserEntity?
    fun findAllByEmailIn(emails: Collection<String>): List<UserEntity>
}
