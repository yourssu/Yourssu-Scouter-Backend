package com.yourssu.scouter.authorization.business

import com.yourssu.scouter.authorization.implement.Role
import com.yourssu.scouter.authorization.implement.UserRoleReader
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val userRoleReader: UserRoleReader,
) {

    fun hasRole(userId: Long, role: Role): Boolean {
        return userRoleReader.hasRole(userId, role)
    }
}
