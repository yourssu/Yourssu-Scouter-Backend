package com.yourssu.scouter.auth.authorization.implement

interface UserRoleRepository {

    fun findAllByUserId(userId: Long): List<UserRole>
    fun existsByUserIdAndRole(userId: Long, role: Role): Boolean
    fun findAllByRole(role: Role): List<UserRole>
    fun countByRole(role: Role): Long
    fun save(userRole: UserRole): UserRole
    fun deleteByUserIdAndRole(userId: Long, role: Role)
}
