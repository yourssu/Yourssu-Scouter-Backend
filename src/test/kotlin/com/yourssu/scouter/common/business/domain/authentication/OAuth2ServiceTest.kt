package com.yourssu.scouter.common.business.domain.authentication

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Handler
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2HandlerComposite
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2TokenInfo
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.user.TokenInfo
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserInfo
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.common.implement.domain.user.UserWriter
import com.yourssu.scouter.common.implement.domain.authentication.TokenProcessor
import com.yourssu.scouter.common.implement.support.exception.CustomException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.Instant

@Suppress("NonAsciiCharacters")
class OAuth2ServiceTest {

    private lateinit var oauth2Service: OAuth2Service
    private val oauth2HandlerComposite = mock<OAuth2HandlerComposite>()
    private val userReader = mock<UserReader>()
    private val userWriter = mock<UserWriter>()
    private val tokenProcessor = mock<TokenProcessor>()

    private fun createUserWithExpiredToken(id: Long = 1L): User {
        return User(
            id = id,
            userInfo = UserInfo(
                name = "test",
                email = "test@example.com",
                profileImageUrl = "",
                oauthId = "oauth-id",
                oauth2Type = OAuth2Type.GOOGLE,
            ),
            tokenInfo = TokenInfo(
                tokenPrefix = "Bearer",
                accessToken = "access",
                refreshToken = "refresh",
                accessTokenExpirationDateTime = Instant.now().minusSeconds(3600),
            ),
        )
    }

    @BeforeEach
    fun setUp() {
        oauth2Service = OAuth2Service(
            oauth2HandlerComposite,
            userReader,
            userWriter,
            tokenProcessor,
        )
    }

    @Nested
    @DisplayName("checkGoogleRefreshTokenValidity")
    inner class CheckGoogleRefreshTokenValidity {

        @Test
        fun `갱신 성공 시 valid true, errorCode null 반환`() {
            val user = createUserWithExpiredToken()
            val mockHandler = mock<OAuth2Handler>()

            whenever(userReader.readById(1L)).thenReturn(user)
            whenever(oauth2HandlerComposite.findHandler(OAuth2Type.GOOGLE)).thenReturn(mockHandler)
            whenever(mockHandler.refreshAccessToken(any())).thenReturn(
                OAuth2TokenInfo(
                    accessToken = "new-access",
                    tokenPrefix = "Bearer",
                    expiresIn = 3600L,
                ),
            )
            whenever(userWriter.write(any<User>())).thenReturn(user)

            val result = oauth2Service.checkGoogleRefreshTokenValidity(1L)

            assertThat(result.valid).isTrue()
            assertThat(result.errorCode).isNull()
        }

        @Test
        fun `CustomException 발생 시 valid false, errorCode 반환`() {
            val user = createUserWithExpiredToken()
            val mockHandler = mock<OAuth2Handler>()

            whenever(userReader.readById(1L)).thenReturn(user)
            whenever(oauth2HandlerComposite.findHandler(OAuth2Type.GOOGLE)).thenReturn(mockHandler)
            whenever(mockHandler.refreshAccessToken(any())).thenThrow(
                CustomException(
                    "OAuth2 토큰 갱신 실패(400)",
                    "OAuth-Token-Refresh-Fail",
                    HttpStatus.UNAUTHORIZED,
                ),
            )

            val result = oauth2Service.checkGoogleRefreshTokenValidity(1L)

            assertThat(result.valid).isFalse()
            assertThat(result.errorCode).isEqualTo("OAuth-Token-Refresh-Fail")
        }
    }
}
