package com.yourssu.scouter.auth.user.implement

import com.yourssu.scouter.auth.authentication.implement.OAuth2User
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
            authType = AuthType.OAUTH2,
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

    fun writeGeneral(name: String, email: String, encodedPassword: String): User {
        val userInfo = UserInfo(
            name = name,
            email = email,
            profileImageUrl = "",
            authType = AuthType.GENERAL,
            password = encodedPassword,
        )

        return userRepository.save(User(userInfo = userInfo))
    }

    fun withdraw(userId: Long) {
        userRepository.deleteById(userId)
    }
}
