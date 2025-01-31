package com.yourssu.scouter.common.implement.domain.user

interface UserRepository {

    fun save(user: User): User
    fun findByOAuthId(oauthId: String): User?
}
