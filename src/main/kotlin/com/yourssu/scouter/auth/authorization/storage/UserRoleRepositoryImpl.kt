package com.yourssu.scouter.auth.authorization.storage

import com.yourssu.scouter.auth.authorization.implement.Role
import com.yourssu.scouter.auth.authorization.implement.UserRole
import com.yourssu.scouter.auth.authorization.implement.UserRoleRepository
import org.springframework.stereotype.Repository

@Repository
class UserRoleRepositoryImpl(
    private val jpaUserRoleRepository: JpaUserRoleRepository,
) : UserRoleRepository {

    override fun findAllByUserId(userId: Long): List<UserRole> {
        return jpaUserRoleRepository.findAllByUserId(userId).map { it.toDomain() }
    }

    override fun existsByUserIdAndRole(userId: Long, role: Role): Boolean {
        return jpaUserRoleRepository.existsByUserIdAndRole(userId, role)
    }
}
