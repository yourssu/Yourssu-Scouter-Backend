package com.yourssu.scouter.auth.authorization.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class UserRoleReader(
    private val userRoleRepository: UserRoleRepository,
) {

    fun readAllByUserId(userId: Long): List<UserRole> {
        return userRoleRepository.findAllByUserId(userId)
    }

    fun hasRole(userId: Long, role: Role): Boolean {
        return userRoleRepository.existsByUserIdAndRole(userId, role)
    }
}
