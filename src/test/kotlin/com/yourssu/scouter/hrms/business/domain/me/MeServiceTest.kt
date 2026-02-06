package com.yourssu.scouter.hrms.business.domain.me

import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import com.yourssu.scouter.common.implement.domain.user.TokenInfo
import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserInfo
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.hrms.business.support.exception.MemberNotRegisteredException
import com.yourssu.scouter.hrms.fixture.MemberFixtureBuilder
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class MeServiceTest {

    private lateinit var meService: MeService
    private lateinit var userReader: UserReader
    private lateinit var memberReader: MemberReader

    @BeforeEach
    fun setUp() {
        userReader = mock(UserReader::class.java)
        memberReader = mock(MemberReader::class.java)
        meService = MeService(userReader, memberReader)
    }

    private fun createUser(email: String = "hong@soongsil.ac.kr", profileImageUrl: String = "https://lh3.googleusercontent.com/photo.jpg") = User(
        id = 1L,
        userInfo = UserInfo(
            name = "홍길동",
            email = email,
            profileImageUrl = profileImageUrl,
            oauthId = "oauth-id-123",
            oauth2Type = OAuth2Type.GOOGLE,
        ),
        tokenInfo = TokenInfo(
            tokenPrefix = "Bearer",
            accessToken = "access-token",
            refreshToken = "refresh-token",
            accessTokenExpiresIn = 3600L,
        ),
    )

    @Nested
    @DisplayName("getMe 메서드는")
    inner class GetMeTests {

        @Test
        fun `등록된 멤버의 정보와 프로필 이미지를 반환한다`() {
            // given
            val user = createUser()
            val member = MemberFixtureBuilder().email("hong@soongsil.ac.kr").build()

            whenever(userReader.readById(1L)).thenReturn(user)
            whenever(memberReader.readByEmailOrNull("hong@soongsil.ac.kr")).thenReturn(member)

            // when
            val result = meService.getMe(1L)

            // then
            assertThat(result.profileImageUrl).isEqualTo("https://lh3.googleusercontent.com/photo.jpg")
            assertThat(result.member.id).isEqualTo(member.id)
            assertThat(result.member.email).isEqualTo("hong@soongsil.ac.kr")
            assertThat(result.member.name).isEqualTo("홍길동")
        }

        @Test
        fun `미등록 사용자이면 MemberNotRegisteredException이 발생한다`() {
            // given
            val user = createUser(email = "unknown@gmail.com")

            whenever(userReader.readById(1L)).thenReturn(user)
            whenever(memberReader.readByEmailOrNull("unknown@gmail.com")).thenReturn(null)

            // when & then
            assertThatThrownBy { meService.getMe(1L) }
                .isInstanceOf(MemberNotRegisteredException::class.java)
                .hasMessageContaining("등록된 멤버가 아닙니다")
        }

        @Test
        fun `응답에 note가 포함되지 않는다`() {
            // given
            val user = createUser()
            val member = MemberFixtureBuilder().email("hong@soongsil.ac.kr").build()

            whenever(userReader.readById(1L)).thenReturn(user)
            whenever(memberReader.readByEmailOrNull("hong@soongsil.ac.kr")).thenReturn(member)

            // when
            val result = meService.getMe(1L)

            // then
            val meResponse = com.yourssu.scouter.hrms.application.domain.me.MeResponse.from(result)
            val fields = meResponse.javaClass.declaredFields.map { it.name }
            assertThat(fields).doesNotContain("note")
        }
    }
}
