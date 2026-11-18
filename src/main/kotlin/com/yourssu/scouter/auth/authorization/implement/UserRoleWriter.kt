package com.yourssu.scouter.auth.authorization.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class UserRoleWriter(
    private val userRoleRepository: UserRoleRepository,
) {

    fun write(userId: Long, role: Role): UserRole {
        return userRoleRepository.save(UserRole(userId = userId, role = role))
    }

    fun delete(userId: Long, role: Role) {
        userRoleRepository.deleteByUserIdAndRole(userId, role)
    }
}
