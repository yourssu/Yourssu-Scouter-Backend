package com.yourssu.scouter.common.implement.domain.user

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class UserReader(
    private val userRepository: UserRepository,
) {

    fun find(oauth2User: OAuth2User): User? {
        val oauthId: String = oauth2User.userInfo.oauthId

        return userRepository.findByOAuthId(oauthId)
    }
}
