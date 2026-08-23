package com.yourssu.scouter.auth.authentication.business

import com.yourssu.scouter.auth.authentication.implement.GeneralAuthValidator
import com.yourssu.scouter.auth.authentication.implement.Token
import com.yourssu.scouter.auth.authentication.implement.TokenProcessor
import com.yourssu.scouter.auth.support.exception.InvalidCredentialsException
import com.yourssu.scouter.auth.user.implement.AuthType
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserInfo
import com.yourssu.scouter.auth.user.implement.UserMemberLinker
import com.yourssu.scouter.auth.user.implement.UserWriter
import com.yourssu.scouter.member.core.fixture.MemberFixtureBuilder
import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.support.exception.MemberNotRegisteredException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder

@Suppress("NonAsciiCharacters")
class GeneralAuthServiceTest {

    private val userWriter = mock<UserWriter>()
    private val userMemberLinker = mock<UserMemberLinker>()
    private val generalAuthValidator = mock<GeneralAuthValidator>()
    private val tokenProcessor = mock<TokenProcessor>()
    private val passwordEncoder = mock<PasswordEncoder>()

    private val generalAuthService = GeneralAuthService(
        userWriter = userWriter,
        userMemberLinker = userMemberLinker,
        generalAuthValidator = generalAuthValidator,
        tokenProcessor = tokenProcessor,
        passwordEncoder = passwordEncoder,
    )

    @Test
    fun `등록된 멤버는 이메일과 비밀번호로 회원가입할 수 있다`() {
        val email = "hong@soongsil.ac.kr"
        val member: Member = MemberFixtureBuilder().email(email).name("홍길동").build()
        whenever(generalAuthValidator.validateSignupEligible(email)).thenReturn(member)
        whenever(passwordEncoder.encode("password1234")).thenReturn("encoded-password")
        whenever(userWriter.writeGeneral(name = "홍길동", email = email, encodedPassword = "encoded-password"))
            .thenReturn(
                User(
                    id = 1L,
                    userInfo = UserInfo(
                        name = "홍길동",
                        email = email,
                        profileImageUrl = "",
                        authType = AuthType.GENERAL,
                        password = "encoded-password",
                    ),
                ),
            )

        val result = generalAuthService.signup(email = email, password = "password1234")

        assertThat(result.id).isEqualTo(1L)
        assertThat(result.email).isEqualTo(email)
        assertThat(result.name).isEqualTo("홍길동")
        verify(userWriter).writeGeneral(name = "홍길동", email = email, encodedPassword = "encoded-password")
    }

    @Test
    fun `가입 자격 검증에 실패하면 유저를 생성하지 않고 예외를 전파한다`() {
        val email = "unknown@soongsil.ac.kr"
        whenever(generalAuthValidator.validateSignupEligible(email))
            .thenThrow(MemberNotRegisteredException("등록된 멤버가 아닙니다."))

        assertThatThrownBy { generalAuthService.signup(email = email, password = "password1234") }
            .isInstanceOf(MemberNotRegisteredException::class.java)

        verify(userWriter, org.mockito.kotlin.never()).writeGeneral(any(), any(), any())
    }

    @Test
    fun `이메일과 비밀번호로 로그인하면 토큰과 멤버 정보를 반환한다`() {
        val email = "hong@soongsil.ac.kr"
        val user = User(
            id = 1L,
            userInfo = UserInfo(
                name = "홍길동",
                email = email,
                profileImageUrl = "",
                authType = AuthType.GENERAL,
                password = "encoded-password",
            ),
        )
        val member: Member = MemberFixtureBuilder().email(email).build()
        whenever(generalAuthValidator.validateLoginCredentials(email, "password1234")).thenReturn(user)
        whenever(tokenProcessor.generateToken(any(), any())).thenReturn(Token("access", "refresh"))
        whenever(userMemberLinker.link(1L, email)).thenReturn(member)

        val result = generalAuthService.login(email = email, password = "password1234")

        assertThat(result.accessToken).isEqualTo("access")
        assertThat(result.refreshToken).isEqualTo("refresh")
        assertThat(result.member.email).isEqualTo(email)
    }

    @Test
    fun `로그인 자격 검증에 실패하면 예외를 전파한다`() {
        val email = "hong@soongsil.ac.kr"
        whenever(generalAuthValidator.validateLoginCredentials(email, "wrong-password"))
            .thenThrow(InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."))

        assertThatThrownBy { generalAuthService.login(email = email, password = "wrong-password") }
            .isInstanceOf(InvalidCredentialsException::class.java)
    }
}
