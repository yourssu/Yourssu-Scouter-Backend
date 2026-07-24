package com.yourssu.scouter.authorization.implement

interface UserRoleRepository {

    fun findAllByUserId(userId: Long): List<UserRole>
    fun existsByUserIdAndRole(userId: Long, role: Role): Boolean
}
