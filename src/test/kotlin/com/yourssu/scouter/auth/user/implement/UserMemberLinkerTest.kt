package com.yourssu.scouter.auth.user.implement

import com.yourssu.scouter.member.core.fixture.MemberFixtureBuilder
import com.yourssu.scouter.member.core.implement.Member
import com.yourssu.scouter.member.core.implement.MemberWriter
import com.yourssu.scouter.member.support.exception.MemberNotRegisteredException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class UserMemberLinkerTest {

    private val memberWriter = mock<MemberWriter>()
    private val userMemberLinker = UserMemberLinker(memberWriter)

    @Test
    fun `등록된 멤버가 있으면 User와 연결한다`() {
        val email = "hong@soongsil.ac.kr"
        val member: Member = MemberFixtureBuilder().email(email).build()
        whenever(memberWriter.updateUserIdByEmail(1L, email)).thenReturn(member)

        val result = userMemberLinker.link(1L, email)

        assertThat(result).isEqualTo(member)
    }

    @Test
    fun `등록된 멤버가 없으면 예외를 던진다`() {
        val email = "unknown@soongsil.ac.kr"
        whenever(memberWriter.updateUserIdByEmail(1L, email)).thenReturn(null)

        assertThatThrownBy { userMemberLinker.link(1L, email) }
            .isInstanceOf(MemberNotRegisteredException::class.java)
    }
}
