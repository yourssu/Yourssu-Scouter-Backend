package com.yourssu.scouter.auth.user.implement

interface UserRepository {

    fun save(user: User): User
    fun existsById(userId: Long): Boolean
    fun findById(userId: Long): User?
    fun findByOAuthId(oauthId: String): User?
    fun findByEmail(email: String): User?
    fun findAllByIds(userIds: Collection<Long>): List<User>
    fun findAllByEmails(emails: Collection<String>): List<User>
    fun deleteById(userId: Long)
}
