package com.yourssu.scouter.auth.authorization.storage

import com.yourssu.scouter.auth.authorization.implement.Role
import org.springframework.data.jpa.repository.JpaRepository

interface JpaUserRoleRepository : JpaRepository<UserRoleEntity, Long> {

    fun findAllByUserId(userId: Long): List<UserRoleEntity>
    fun existsByUserIdAndRole(userId: Long, role: Role): Boolean
    fun findAllByRole(role: Role): List<UserRoleEntity>
    fun findAllByUserIdAndRole(userId: Long, role: Role): List<UserRoleEntity>
    fun countByRole(role: Role): Long
}
