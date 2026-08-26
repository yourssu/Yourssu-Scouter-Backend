package com.yourssu.scouter.auth.authorization.business

import com.yourssu.scouter.auth.authorization.implement.Role
import com.yourssu.scouter.auth.authorization.implement.UserRoleReader
import org.springframework.stereotype.Service

@Service
class AuthorizationService(
    private val userRoleReader: UserRoleReader,
) {

    fun hasRole(userId: Long, role: Role): Boolean {
        return userRoleReader.hasRole(userId, role)
    }
}
