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
import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.slf4j.LoggerFactory
import java.time.Instant
import org.springframework.stereotype.Service

@Service
class OAuth2Service(
    private val oauth2HandlerComposite: OAuth2HandlerComposite,
    private val userReader: UserReader,
    private val userWriter: UserWriter,
    private val tokenProcessor: TokenProcessor,
) {

    fun getAuthCodeRequestUrl(oauth2Type: OAuth2Type, referer: String): String {
        val oauth2Handler: OAuth2Handler = oauth2HandlerComposite.findHandler(oauth2Type)

        return oauth2Handler.provideAuthCodeRequestUrl(referer)
    }

    fun login(
        oauth2Type: OAuth2Type,
        oauth2AuthorizationCode: String,
        referer: String,
        redirectUriOverride: String?
    ): LoginResult {
        val oauth2User: OAuth2User = fetchOAuth2User(
            oauth2Type = oauth2Type,
            authorizationCode = oauth2AuthorizationCode,
            referer = referer,
            redirectUriOverride = redirectUriOverride,
        )
        val loginUser: User = createOrUpdate(oauth2User)

        val tokenIssueTime = Instant.now()
        val privateClaims = PrivateClaims(loginUser.id!!)
        val token: Token = tokenProcessor.generateToken(
            issueTime = tokenIssueTime,
            privateClaims = privateClaims.toMap()
        )

        return LoginResult(
            id = loginUser.id,
            accessToken = token.accessToken,
            refreshToken = token.refreshToken,
            email = loginUser.getEmailAddress(),
            profileImageUrl = loginUser.userInfo.profileImageUrl,
        )
    }

    private fun fetchOAuth2User(
        oauth2Type: OAuth2Type,
        authorizationCode: String,
        referer: String,
        redirectUriOverride: String?,
    ): OAuth2User {
        val oauth2Handler: OAuth2Handler = oauth2HandlerComposite.findHandler(oauth2Type)

        return oauth2Handler.fetchOAuth2User(authorizationCode, referer, redirectUriOverride)
    }

    private fun createOrUpdate(oauth2User: OAuth2User): User {
        val findUser: User? = userReader.find(oauth2User)
        if (findUser != null) {
            findUser.updateToken(oauth2User.token)
            userWriter.write(findUser)
            return findUser
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

    private val log = LoggerFactory.getLogger(OAuth2Service::class.java)

    /**
     * 현재 사용자의 Google OAuth2 refresh token이 유효한지 검사한다.
     * 실제로 갱신을 시도하고, 성공 시 발급된 access token을 DB에 저장한다.
     * 실패 시 [OAuth2RefreshTokenCheckResult]에 errorCode를 담아 반환한다.
     * access token 만료 여부와 무관하게 항상 Google에 refresh를 시도해, refresh_token 유효 여부만 검사한다.
     * 메일 예약 발송, 구글 폼/드라이브 동기화 등 Google API 연동 기능에서 재사용할 수 있다.
     */
    fun checkGoogleRefreshTokenValidity(userId: Long): OAuth2RefreshTokenCheckResult {
        return try {
            val user: User = userReader.readById(userId)
            val newTokenInfo: OAuth2TokenInfo = refreshAccessToken(OAuth2Type.GOOGLE, user.getBearerRefreshToken())
            user.updateToken(newTokenInfo)
            userWriter.write(user)
            OAuth2RefreshTokenCheckResult(valid = true, errorCode = null)
        } catch (e: CustomException) {
            OAuth2RefreshTokenCheckResult(valid = false, errorCode = e.errorCode)
        } catch (e: Exception) {
            log.warn("Google refresh token 검사 중 예상치 못한 예외: userId={}", userId, e)
            OAuth2RefreshTokenCheckResult(valid = false, errorCode = "OAuth-Token-Refresh-Fail")
        }
    }
}
