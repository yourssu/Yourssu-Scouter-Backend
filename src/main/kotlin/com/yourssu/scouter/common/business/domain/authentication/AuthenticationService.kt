package com.yourssu.scouter.common.business.domain.authentication

import com.yourssu.scouter.common.business.support.exception.NoSuchUserException
import com.yourssu.scouter.common.implement.domain.authentication.BlacklistTokenReader
import com.yourssu.scouter.common.implement.domain.authentication.BlacklistTokenWriter
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2TokenInfo
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2User
import com.yourssu.scouter.common.implement.domain.authentication.PrivateClaims
import com.yourssu.scouter.common.implement.domain.authentication.TokenProcessor
import com.yourssu.scouter.common.implement.domain.authentication.TokenType
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.domain.user.UserWriter
import com.yourssu.scouter.common.implement.support.exception.InvalidTokenException
import io.jsonwebtoken.Claims
import java.time.LocalDateTime
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    private val oauth2Service: OAuth2Service,
    private val userReader: UserReader,
    private val userWriter: UserWriter,
    private val tokenProcessor: TokenProcessor,
    private val blacklistTokenWriter: BlacklistTokenWriter,
    private val blacklistTokenReader: BlacklistTokenReader,
) {

    fun login(oauth2Type: OAuth2Type, oauth2AuthorizationCode: String): LoginResult {
        val oauth2User: OAuth2User = oauth2Service.fetchOAuth2User(oauth2Type, oauth2AuthorizationCode)
        val loginUser: User = createOrUpdate(oauth2User)

        val tokenIssueTime = LocalDateTime.now()
        val privateClaims = PrivateClaims(loginUser.id!!)
        val token: TokenDto = generateTokens(tokenIssueTime, privateClaims)

        return LoginResult(
            id = loginUser.id,
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
        )
    }

    private fun createOrUpdate(oauth2User: OAuth2User): User {
        val findUser: User? = userReader.find(oauth2User)
        if (findUser != null) {
            findUser.updateToken(oauth2User.token)
            userWriter.write(findUser)
        }

        return userWriter.write(oauth2User)
    }

    private fun generateTokens(tokenIssueTime: LocalDateTime, privateClaims: PrivateClaims): TokenDto {
        val accessToken: String = tokenProcessor.encode(
            issueTime = tokenIssueTime,
            tokenType = TokenType.ACCESS,
            privateClaims = privateClaims.toMap()
        )
        val refreshToken: String = tokenProcessor.encode(
            issueTime = tokenIssueTime,
            tokenType = TokenType.REFRESH,
            privateClaims = privateClaims.toMap()
        )

        return TokenDto(accessToken, refreshToken)
    }

    fun refreshOAuth2TokenBeforeExpiry(userId: Long, oauth2Type: OAuth2Type, thresholdMinutes: Long): User {
        val user: User = userReader.readById(userId)
        if (user.isAccessTokenRemainMoreThan(thresholdMinutes)) {
            return user
        }

        val newTokenInfo: OAuth2TokenInfo = oauth2Service.refreshAccessToken(oauth2Type, user.getBearerRefreshToken())
        user.updateToken(newTokenInfo)

        return userWriter.write(user)
    }

    fun logout(accessToken: String, refreshToken: String) {
        val userId = getValidUserId(TokenType.ACCESS, accessToken)

        blacklistTokenWriter.write(userId, accessToken, refreshToken)
    }

    private fun getValidUserId(tokenType: TokenType, token: String): Long {
        val privateClaims: PrivateClaims = decode(tokenType, token)
        val userId: Long = privateClaims.userId
        if (!userReader.existsById(userId)) {
            throw NoSuchUserException("존재하지 않는 유저의 토큰입니다.")
        }
        if (blacklistTokenReader.isBlacklisted(userId, token)) {
            throw InvalidTokenException("로그아웃되었습니다.")
        }

        return userId
    }

    private fun decode(tokenType: TokenType, accessToken: String): PrivateClaims {
        val claims: Claims = tokenProcessor.decode(tokenType, accessToken)
            ?: throw InvalidTokenException("유효한 토큰이 아닙니다.")

        return PrivateClaims.from(claims)
    }

    fun isValidToken(tokenType: TokenType, targetToken: String): Boolean {
        val claims: Claims = tokenProcessor.decode(tokenType, targetToken)
            ?: return false

        val userId = PrivateClaims.from(claims).userId

        return userReader.existsById(userId) &&
                !blacklistTokenReader.isBlacklisted(userId, targetToken)
    }

    fun refreshToken(requestTime: LocalDateTime, refreshToken: String): TokenDto {
        val privateClaims = decode(TokenType.REFRESH, refreshToken)

        return generateTokens(requestTime, privateClaims)
    }
}
