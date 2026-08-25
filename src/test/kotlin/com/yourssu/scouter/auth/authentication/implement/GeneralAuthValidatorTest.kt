package com.yourssu.scouter.auth.authentication.implement

import com.yourssu.scouter.auth.support.exception.DuplicateEmailException
import com.yourssu.scouter.auth.support.exception.InvalidCredentialsException
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserInfo
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.member.core.fixture.MemberFixtureBuilder
import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.member.support.exception.MemberNotRegisteredException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder

@Suppress("NonAsciiCharacters")
class GeneralAuthValidatorTest {

    private val userReader = mock<UserReader>()
    private val memberReader = mock<MemberReader>()
    private val passwordEncoder = mock<PasswordEncoder>()

    private val generalAuthValidator = GeneralAuthValidator(
        userReader = userReader,
        memberReader = memberReader,
        passwordEncoder = passwordEncoder,
    )

    private fun generalUser(email: String, encodedPassword: String) = User(
        id = 1L,
        userInfo = UserInfo(
            name = "홍길동",
            email = email,
            profileImageUrl = "",
            authType = AuthType.GENERAL,
            password = encodedPassword,
        ),
    )

    @Test
    fun `등록된 멤버이면서 이메일이 중복되지 않으면 멤버를 반환한다`() {
        val email = "hong@soongsil.ac.kr"
        val member: Member = MemberFixtureBuilder().email(email).build()
        whenever(memberReader.readByEmailOrNull(email)).thenReturn(member)
        whenever(userReader.findByEmail(email)).thenReturn(null)

        val result = generalAuthValidator.validateSignupEligible(email)

        assertThat(result).isEqualTo(member)
    }

    @Test
    fun `등록된 멤버가 아니면 예외를 던진다`() {
        val email = "unknown@soongsil.ac.kr"
        whenever(memberReader.readByEmailOrNull(email)).thenReturn(null)

        assertThatThrownBy { generalAuthValidator.validateSignupEligible(email) }
            .isInstanceOf(MemberNotRegisteredException::class.java)
    }

    @Test
    fun `이미 가입된 이메일이면 예외를 던진다`() {
        val email = "hong@soongsil.ac.kr"
        val member: Member = MemberFixtureBuilder().email(email).build()
        whenever(memberReader.readByEmailOrNull(email)).thenReturn(member)
        whenever(userReader.findByEmail(email)).thenReturn(generalUser(email, "already-encoded"))

        assertThatThrownBy { generalAuthValidator.validateSignupEligible(email) }
            .isInstanceOf(DuplicateEmailException::class.java)
    }

    @Test
    fun `이메일과 비밀번호가 일치하면 유저를 반환한다`() {
        val email = "hong@soongsil.ac.kr"
        val user = generalUser(email, "encoded-password")
        whenever(userReader.findByEmail(email)).thenReturn(user)
        whenever(passwordEncoder.matches("password1234", "encoded-password")).thenReturn(true)

        val result = generalAuthValidator.validateLoginCredentials(email, "password1234")

        assertThat(result).isEqualTo(user)
    }

    @Test
    fun `가입되지 않은 이메일로 로그인하면 예외를 던진다`() {
        val email = "unknown@soongsil.ac.kr"
        whenever(userReader.findByEmail(email)).thenReturn(null)

        assertThatThrownBy { generalAuthValidator.validateLoginCredentials(email, "password1234") }
            .isInstanceOf(InvalidCredentialsException::class.java)
    }

    @Test
    fun `비밀번호가 일치하지 않으면 예외를 던진다`() {
        val email = "hong@soongsil.ac.kr"
        val user = generalUser(email, "encoded-password")
        whenever(userReader.findByEmail(email)).thenReturn(user)
        whenever(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false)

        assertThatThrownBy { generalAuthValidator.validateLoginCredentials(email, "wrong-password") }
            .isInstanceOf(InvalidCredentialsException::class.java)
    }

    @Test
    fun `OAuth 유저는 일반 로그인할 수 없다`() {
        val email = "oauth@soongsil.ac.kr"
        val oauthUser = User(
            id = 2L,
            userInfo = UserInfo(
                name = "구글유저",
                email = email,
                profileImageUrl = "",
                authType = AuthType.OAUTH2,
                oauthId = "oauth-id",
            ),
        )
        whenever(userReader.findByEmail(email)).thenReturn(oauthUser)

        assertThatThrownBy { generalAuthValidator.validateLoginCredentials(email, "password1234") }
            .isInstanceOf(InvalidCredentialsException::class.java)
    }
}
