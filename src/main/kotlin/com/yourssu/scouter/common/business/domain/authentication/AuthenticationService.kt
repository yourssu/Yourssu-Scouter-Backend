package com.yourssu.scouter.common.business.domain.authentication

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2User
import com.yourssu.scouter.common.implement.domain.authentication.PrivateClaims
import com.yourssu.scouter.common.implement.domain.authentication.TokenProcessor
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.domain.user.UserWriter
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val oauth2Service: OAuth2Service,
    private val userReader: UserReader,
    private val userWriter: UserWriter,
    private val tokenProcessor: TokenProcessor,
) {

    fun login(oauth2Type: OAuth2Type, oauth2AuthorizationCode: String): LoginResult {
        val oauth2User = oauth2Service.fetchOAuth2User(oauth2Type, oauth2AuthorizationCode)
        val loginUser: User = createOrUpdate(oauth2User)
        val token: TokenDto = generateTokens(loginUser)

        return LoginResult(
            id = loginUser.id!!,
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
        )
    }

    private fun generateTokens(user: User): TokenDto {
        val privateClaims = PrivateClaims(user.id!!)
        val tokenIssueTime = LocalDateTime.now()

        val accessToken = tokenProcessor.encode(
            issueTime = tokenIssueTime,
            tokenType = TokenType.ACCESS,
            privateClaims = privateClaims.toMap(),
        )

        val refreshToken = tokenProcessor.encode(
            issueTime = tokenIssueTime,
            tokenType = TokenType.REFRESH,
            privateClaims = privateClaims.toMap(),
        )
        return TokenDto(accessToken, refreshToken)
    }

    private fun createOrUpdate(oauth2User: OAuth2User): User {
        val findUser: User? = userReader.find(oauth2User)
        if (findUser != null) {
            findUser.updateToken(oauth2User.token)
            userWriter.write(findUser)
        }

        return userWriter.write(oauth2User)
    }
}

data class TokenDto(
    val accessToken: String,
    val refreshToken: String,
)
