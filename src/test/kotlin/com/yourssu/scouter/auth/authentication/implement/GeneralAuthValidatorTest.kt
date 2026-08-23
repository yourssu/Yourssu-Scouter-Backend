package com.yourssu.scouter.auth.authentication.implement

import com.yourssu.scouter.auth.support.exception.DuplicateEmailException
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

@Suppress("NonAsciiCharacters")
class GeneralAuthValidatorTest {

    private val userReader = mock<UserReader>()
    private val memberReader = mock<MemberReader>()

    private val generalAuthValidator = GeneralAuthValidator(
        userReader = userReader,
        memberReader = memberReader,
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
        whenever(userReader.findByEmail(email)).thenReturn(
            User(
                id = 1L,
                userInfo = UserInfo(
                    name = "홍길동",
                    email = email,
                    profileImageUrl = "",
                    authType = AuthType.GENERAL,
                    password = "already-encoded",
                ),
            ),
        )

        assertThatThrownBy { generalAuthValidator.validateSignupEligible(email) }
            .isInstanceOf(DuplicateEmailException::class.java)
    }
}
