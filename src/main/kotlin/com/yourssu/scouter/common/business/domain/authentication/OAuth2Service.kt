package com.yourssu.scouter.common.business.domain.authentication

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Handler
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2HandlerComposite
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2TokenInfo
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2User
import com.yourssu.scouter.common.implement.domain.authentication.PrivateClaims
import com.yourssu.scouter.common.implement.domain.authentication.Token
import com.yourssu.scouter.common.implement.domain.authentication.TokenProcessor
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.domain.user.UserWriter
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class OAuth2Service(
    private val oauth2HandlerComposite: OAuth2HandlerComposite,
    private val userReader: UserReader,
    private val userWriter: UserWriter,
    private val tokenProcessor: TokenProcessor,
) {

    fun getAuthCodeRequestUrl(oauth2Type: OAuth2Type): String {
        val oauth2Handler: OAuth2Handler = oauth2HandlerComposite.findHandler(oauth2Type)

        return oauth2Handler.provideAuthCodeRequestUrl()
    }

    fun login(oauth2Type: OAuth2Type, oauth2AuthorizationCode: String): LoginResult {
        val oauth2User: OAuth2User = fetchOAuth2User(oauth2Type, oauth2AuthorizationCode)
        val loginUser: User = createOrUpdate(oauth2User)

        val tokenIssueTime = LocalDateTime.now()
        val privateClaims = PrivateClaims(loginUser.id!!)
        val token: Token = tokenProcessor.generateToken(
            issueTime = tokenIssueTime,
            privateClaims = privateClaims.toMap()
        )

        return LoginResult(
            id = loginUser.id,
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
        )
    }

    private fun fetchOAuth2User(oauth2Type: OAuth2Type, authorizationCode: String): OAuth2User {
        val oauth2Handler: OAuth2Handler = oauth2HandlerComposite.findHandler(oauth2Type)

        return oauth2Handler.fetchOAuth2User(authorizationCode)
    }

    private fun createOrUpdate(oauth2User: OAuth2User): User {
        val findUser: User? = userReader.find(oauth2User)
        if (findUser != null) {
            findUser.updateToken(oauth2User.token)
            userWriter.write(findUser)
        }

        return userWriter.write(oauth2User)
    }

    fun refreshOAuth2TokenBeforeExpiry(userId: Long, oauth2Type: OAuth2Type, thresholdMinutes: Long): User {
        val user: User = userReader.readById(userId)
        if (user.isAccessTokenRemainMoreThan(thresholdMinutes)) {
            return user
        }

        val newTokenInfo: OAuth2TokenInfo = refreshAccessToken(oauth2Type, user.getBearerRefreshToken())
        user.updateToken(newTokenInfo)

        return userWriter.write(user)
    }

    private fun refreshAccessToken(oauth2Type: OAuth2Type, bearerRefreshToken: String): OAuth2TokenInfo {
        val oauth2Handler: OAuth2Handler = oauth2HandlerComposite.findHandler(oauth2Type)

        return oauth2Handler.refreshAccessToken(bearerRefreshToken)
    }
}
