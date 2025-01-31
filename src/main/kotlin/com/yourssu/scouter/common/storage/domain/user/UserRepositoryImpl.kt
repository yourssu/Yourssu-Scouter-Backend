package com.yourssu.scouter.common.storage.domain.user

import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserRepository
import org.springframework.stereotype.Repository

@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository
) : UserRepository {

    override fun save(user: User): User {
        return jpaUserRepository.save(UserEntity.from(user)).toDomain()
    }

    override fun findByOAuthId(oauthId: String): User? {
        return jpaUserRepository.findByOauthId(oauthId)?.toDomain()
    }
}
