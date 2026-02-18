package com.yourssu.scouter.hrms.business.domain.authentication

import com.yourssu.scouter.common.business.domain.authentication.LoginResult
import com.yourssu.scouter.common.business.domain.authentication.OAuth2Service
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.business.support.exception.MemberNotRegisteredException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class LoginServiceTest {

    private lateinit var loginService: LoginService
    private lateinit var oauth2Service: OAuth2Service
    private lateinit var memberReader: MemberReader

    @BeforeEach
    fun setUp() {
        oauth2Service = mock(OAuth2Service::class.java)
        memberReader = mock(MemberReader::class.java)
        loginService = LoginService(oauth2Service, memberReader)
    }

    private fun createLoginResult(email: String = "hong@soongsil.ac.kr") = LoginResult(
        id = 1L,
        accessToken = "access-token",
        refreshToken = "refresh-token",
        email = email,
        profileImageUrl = "https://lh3.googleusercontent.com/photo.jpg",
    )

    @Nested
    @DisplayName("login 메서드는")
    inner class LoginTests {

        @Test
        fun `등록된 멤버로 로그인하면 토큰과 멤버 정보를 반환한다`() {
            // given
            val loginResult = createLoginResult()
            val member = MemberFixtureBuilder().email("hong@soongsil.ac.kr").build()

            whenever(
                oauth2Service.login(
                    oauth2Type = OAuth2Type.GOOGLE,
                    oauth2AuthorizationCode = "auth-code",
                    referer = "http://localhost:3000",
                    redirectUriOverride = null,
                )
            ).thenReturn(loginResult)
            whenever(memberReader.readByEmailOrNull("hong@soongsil.ac.kr")).thenReturn(member)

            // when
            val result = loginService.login(
                oauth2Type = OAuth2Type.GOOGLE,
                oauth2AuthorizationCode = "auth-code",
                referer = "http://localhost:3000",
                redirectUriOverride = null,
            )

            // then
            assertThat(result.accessToken).isEqualTo("access-token")
            assertThat(result.refreshToken).isEqualTo("refresh-token")
            assertThat(result.profileImageUrl).isEqualTo("https://lh3.googleusercontent.com/photo.jpg")
            assertThat(result.member.id).isEqualTo(member.id)
            assertThat(result.member.email).isEqualTo("hong@soongsil.ac.kr")
            assertThat(result.member.name).isEqualTo("홍길동")
        }

        @Test
        fun `미등록 사용자로 로그인하면 MemberNotRegisteredException이 발생한다`() {
            // given
            val loginResult = createLoginResult(email = "unknown@gmail.com")

            whenever(
                oauth2Service.login(
                    oauth2Type = OAuth2Type.GOOGLE,
                    oauth2AuthorizationCode = "auth-code",
                    referer = "http://localhost:3000",
                    redirectUriOverride = null,
                )
            ).thenReturn(loginResult)
            whenever(memberReader.readByEmailOrNull("unknown@gmail.com")).thenReturn(null)

            // when & then
            assertThatThrownBy {
                loginService.login(
                    oauth2Type = OAuth2Type.GOOGLE,
                    oauth2AuthorizationCode = "auth-code",
                    referer = "http://localhost:3000",
                    redirectUriOverride = null,
                )
            }.isInstanceOf(MemberNotRegisteredException::class.java)
                .hasMessageContaining("등록된 멤버가 아닙니다")
        }

        @Test
        fun `로그인 응답의 멤버 정보에 note가 포함되지 않는다`() {
            // given
            val loginResult = createLoginResult()
            val member = MemberFixtureBuilder().email("hong@soongsil.ac.kr").build()

            whenever(
                oauth2Service.login(
                    oauth2Type = OAuth2Type.GOOGLE,
                    oauth2AuthorizationCode = "auth-code",
                    referer = "http://localhost:3000",
                    redirectUriOverride = null,
                )
            ).thenReturn(loginResult)
            whenever(memberReader.readByEmailOrNull("hong@soongsil.ac.kr")).thenReturn(member)

            // when
            val result = loginService.login(
                oauth2Type = OAuth2Type.GOOGLE,
                oauth2AuthorizationCode = "auth-code",
                referer = "http://localhost:3000",
                redirectUriOverride = null,
            )

            // then
            val meResponse = com.yourssu.scouter.hrms.application.domain.me.MeResponse.from(
                com.yourssu.scouter.hrms.business.domain.me.MeResult(
                    profileImageUrl = result.profileImageUrl,
                    member = result.member,
                )
            )
            val fields = meResponse.javaClass.declaredFields.map { it.name }
            assertThat(fields).doesNotContain("note")
        }

        @Test
        fun `로그인 응답에 profileImageUrl이 포함된다`() {
            // given
            val profileUrl = "https://lh3.googleusercontent.com/custom-photo.jpg"
            val loginResult = createLoginResult().copy(profileImageUrl = profileUrl)
            val member = MemberFixtureBuilder().email("hong@soongsil.ac.kr").build()

            whenever(
                oauth2Service.login(
                    oauth2Type = OAuth2Type.GOOGLE,
                    oauth2AuthorizationCode = "auth-code",
                    referer = "http://localhost:3000",
                    redirectUriOverride = null,
                )
            ).thenReturn(loginResult)
            whenever(memberReader.readByEmailOrNull("hong@soongsil.ac.kr")).thenReturn(member)

            // when
            val result = loginService.login(
                oauth2Type = OAuth2Type.GOOGLE,
                oauth2AuthorizationCode = "auth-code",
                referer = "http://localhost:3000",
                redirectUriOverride = null,
            )

            // then
            assertThat(result.profileImageUrl).isEqualTo(profileUrl)
        }
    }
}
