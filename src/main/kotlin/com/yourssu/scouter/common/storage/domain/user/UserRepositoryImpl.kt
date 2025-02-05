package com.yourssu.scouter.common.storage.domain.user

import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository
) : UserRepository {

    override fun save(user: User): User {
        return jpaUserRepository.save(UserEntity.from(user)).toDomain()
    }

    override fun existsById(userId: Long): Boolean {
        return jpaUserRepository.existsById(userId)
    }

    override fun findById(userId: Long): User? {
        return jpaUserRepository.findByIdOrNull(userId)?.toDomain()
    }

    override fun findByOAuthId(oauthId: String): User? {
        return jpaUserRepository.findByOauthId(oauthId)?.toDomain()
    }

    override fun deleteById(userId: Long) {
        jpaUserRepository.deleteById(userId)
    }
}
