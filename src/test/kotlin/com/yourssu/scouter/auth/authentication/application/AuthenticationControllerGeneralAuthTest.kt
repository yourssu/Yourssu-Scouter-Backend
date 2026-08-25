package com.yourssu.scouter.auth.authentication.application

import com.yourssu.scouter.auth.authentication.application.dto.LoginRequest
import com.yourssu.scouter.auth.authentication.application.dto.SignupRequest
import com.yourssu.scouter.auth.authentication.business.AuthenticationService
import com.yourssu.scouter.auth.authentication.business.GeneralAuthService
import com.yourssu.scouter.auth.authentication.business.LoginService
import com.yourssu.scouter.auth.authentication.business.dto.LoginWithMemberResult
import com.yourssu.scouter.auth.authentication.business.dto.SignupResult
import com.yourssu.scouter.auth.support.exception.DuplicateEmailException
import com.yourssu.scouter.auth.support.exception.InvalidCredentialsException
import com.yourssu.scouter.member.core.business.dto.MemberDto
import com.yourssu.scouter.member.core.fixture.MemberFixtureBuilder
import com.yourssu.scouter.member.support.exception.MemberNotRegisteredException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class AuthenticationControllerGeneralAuthTest {

    private val authenticationService: AuthenticationService = mock()
    private val loginService: LoginService = mock()
    private val generalAuthService: GeneralAuthService = mock()

    private val controller = AuthenticationController(
        authenticationService = authenticationService,
        loginService = loginService,
        generalAuthService = generalAuthService,
    )

    @Test
    fun `signup은 이메일과 비밀번호로 가입해 200과 회원 정보를 반환한다`() {
        val request = SignupRequest(email = "hong@soongsil.ac.kr", password = "password1234")
        whenever(generalAuthService.signup(email = request.email, password = request.password))
            .thenReturn(SignupResult(id = 1L, email = request.email, name = "홍길동"))

        val response = controller.signup(request)

        assertThat(response.statusCode.value()).isEqualTo(200)
        assertThat(response.body?.id).isEqualTo(1L)
        assertThat(response.body?.email).isEqualTo(request.email)
        assertThat(response.body?.name).isEqualTo("홍길동")
    }

    @Test
    fun `signup은 등록되지 않은 멤버면 예외를 전파한다`() {
        val request = SignupRequest(email = "unknown@soongsil.ac.kr", password = "password1234")
        whenever(generalAuthService.signup(email = request.email, password = request.password))
            .thenThrow(MemberNotRegisteredException("등록된 멤버가 아닙니다."))

        assertThatThrownBy { controller.signup(request) }
            .isInstanceOf(MemberNotRegisteredException::class.java)
    }

    @Test
    fun `signup은 이미 가입된 이메일이면 예외를 전파한다`() {
        val request = SignupRequest(email = "hong@soongsil.ac.kr", password = "password1234")
        whenever(generalAuthService.signup(email = request.email, password = request.password))
            .thenThrow(DuplicateEmailException("이미 가입된 이메일입니다."))

        assertThatThrownBy { controller.signup(request) }
            .isInstanceOf(DuplicateEmailException::class.java)
    }

    @Test
    fun `login은 이메일과 비밀번호로 로그인해 200과 토큰을 반환한다`() {
        val request = LoginRequest(email = "hong@soongsil.ac.kr", password = "password1234")
        val member: MemberDto = MemberDto.from(MemberFixtureBuilder().email(request.email).build())
        whenever(generalAuthService.login(email = request.email, password = request.password))
            .thenReturn(
                LoginWithMemberResult(
                    accessToken = "access-token",
                    refreshToken = "refresh-token",
                    profileImageUrl = "",
                    member = member,
                ),
            )

        val response = controller.login(request)

        assertThat(response.statusCode.value()).isEqualTo(200)
        assertThat(response.body?.accessToken).isEqualTo("access-token")
        assertThat(response.body?.refreshToken).isEqualTo("refresh-token")
        assertThat(response.body?.tokenType).isEqualTo("Bearer")
    }

    @Test
    fun `login은 이메일 또는 비밀번호가 틀리면 예외를 전파한다`() {
        val request = LoginRequest(email = "hong@soongsil.ac.kr", password = "wrong-password")
        whenever(generalAuthService.login(email = request.email, password = request.password))
            .thenThrow(InvalidCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."))

        assertThatThrownBy { controller.login(request) }
            .isInstanceOf(InvalidCredentialsException::class.java)
    }
}
