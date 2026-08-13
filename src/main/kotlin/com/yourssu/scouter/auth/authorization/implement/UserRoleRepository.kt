package com.yourssu.scouter.auth.authorization.implement

interface UserRoleRepository {

    fun findAllByUserId(userId: Long): List<UserRole>
    fun existsByUserIdAndRole(userId: Long, role: Role): Boolean
}
