package com.yourssu.scouter.auth.authentication.implement

import com.yourssu.scouter.auth.support.exception.DuplicateEmailException
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserInfo
import com.yourssu.scouter.auth.user.implement.UserReader
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class OAuth2SignupValidatorTest {

    private val userReader = mock<UserReader>()

    private val oauth2SignupValidator = OAuth2SignupValidator(userReader = userReader)

    private fun user(email: String, authType: AuthType) = User(
        id = 1L,
        userInfo = UserInfo(
            name = "홍길동",
            email = email,
            profileImageUrl = "",
            authType = authType,
            password = if (authType == AuthType.GENERAL) "encoded-password" else null,
            oauthId = if (authType == AuthType.OAUTH2) "oauth-id" else null,
        ),
    )

    @Test
    fun `같은 이메일의 유저가 없으면 통과한다`() {
        val email = "hong@soongsil.ac.kr"
        whenever(userReader.findByEmail(email)).thenReturn(null)

        assertThatCode { oauth2SignupValidator.validateEmailNotTaken(email) }
            .doesNotThrowAnyException()
    }

    @Test
    fun `이미 일반 회원가입으로 쓰이고 있는 이메일이면 예외를 던진다`() {
        val email = "hong@soongsil.ac.kr"
        whenever(userReader.findByEmail(email)).thenReturn(user(email, AuthType.GENERAL))

        assertThatThrownBy { oauth2SignupValidator.validateEmailNotTaken(email) }
            .isInstanceOf(DuplicateEmailException::class.java)
            .hasMessageContaining("일반 로그인")
    }

    @Test
    fun `이미 다른 소셜 계정이 쓰고 있는 이메일이면 예외를 던진다`() {
        val email = "hong@soongsil.ac.kr"
        whenever(userReader.findByEmail(email)).thenReturn(user(email, AuthType.OAUTH2))

        assertThatThrownBy { oauth2SignupValidator.validateEmailNotTaken(email) }
            .isInstanceOf(DuplicateEmailException::class.java)
            .hasMessageContaining("소셜 계정")
    }
}
