package com.yourssu.scouter.common.implement.domain.user

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class UserWriter(
    private val userRepository: UserRepository,
) {

    fun write(oauth2User: OAuth2User): User {
        val userInfo = UserInfo(
            name = oauth2User.userInfo.name,
            email = oauth2User.userInfo.email,
            profileImageUrl = oauth2User.userInfo.profileImageUrl,
            oauthId = oauth2User.userInfo.oauthId,
            oauth2Type = oauth2User.userInfo.oauth2Type,
        )

        val tokenInfo = TokenInfo(
            tokenPrefix = oauth2User.token.tokenPrefix,
            accessToken = oauth2User.token.accessToken,
            refreshToken = oauth2User.token.refreshToken ?: "",
            accessTokenExpiresIn = oauth2User.token.expiresIn,
        )

        val toSave = User(
            userInfo = userInfo,
            tokenInfo = tokenInfo,
        )

        return userRepository.save(toSave)
    }

    fun write(user: User): User {
        return userRepository.save(user)
    }
}
