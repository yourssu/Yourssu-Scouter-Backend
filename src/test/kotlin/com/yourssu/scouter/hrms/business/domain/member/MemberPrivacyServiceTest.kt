package com.yourssu.scouter.hrms.business.domain.member

import com.yourssu.scouter.common.implement.domain.user.User
import com.yourssu.scouter.common.implement.domain.user.UserInfo
import com.yourssu.scouter.common.implement.domain.user.UserReader
import com.yourssu.scouter.hrms.implement.domain.member.Member
import com.yourssu.scouter.hrms.implement.domain.member.MemberReader
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.division.Division
import com.yourssu.scouter.common.implement.domain.authentication.OAuth2Type
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

class MemberPrivacyServiceTest {

    private val userReader: UserReader = mock()
    private val memberReader: MemberReader = mock()

    private val service = MemberPrivacyService(
        userReader = userReader,
        memberReader = memberReader,
    )

    @Test
    fun `화이트리스트 이메일이면 멤버 정보와 무관하게 privileged로 판단한다`() {
        // given
        val userId = 1L
        val user = createUser(
            id = userId,
            email = "umi.urssu@gmail.com",
        )
        whenever(userReader.readById(userId)).thenReturn(user)

        // when
        val result = service.isPrivilegedUser(userId)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `HR 파트가 있으면 privileged로 판단한다`() {
        // given
        val userId = 2L
        val email = "member@yourssu.com"
        val user = createUser(
            id = userId,
            email = email,
        )
        whenever(userReader.readById(userId)).thenReturn(user)

        val member = createMemberWithParts(
            email = email,
            partNames = setOf("Backend", "HR"),
        )
        whenever(memberReader.readByEmailOrNull(email)).thenReturn(member)

        // when
        val result = service.isPrivilegedUser(userId)

        // then
        assertThat(result).isTrue()
    }

    @Test
    fun `화이트리스트도 아니고 HR 파트도 없으면 privileged가 아니다`() {
        // given
        val userId = 3L
        val email = "normal@yourssu.com"
        val user = createUser(
            id = userId,
            email = email,
        )
        whenever(userReader.readById(userId)).thenReturn(user)

        val member = createMemberWithParts(
            email = email,
            partNames = setOf("Backend", "Design"),
        )
        whenever(memberReader.readByEmailOrNull(email)).thenReturn(member)

        // when
        val result = service.isPrivilegedUser(userId)

        // then
        assertThat(result).isFalse()
    }

    private fun createUser(
        id: Long,
        email: String,
    ): User {
        val userInfo = UserInfo(
            name = "name",
            email = email,
            profileImageUrl = "http://example.com/profile.png",
            oauthId = "oauth-id",
            oauth2Type = OAuth2Type.GOOGLE,
        )
        val tokenInfo = com.yourssu.scouter.common.implement.domain.user.TokenInfo(
            tokenPrefix = "Bearer",
            accessToken = "access",
            refreshToken = "refresh",
            accessTokenExpirationDateTime = Instant.now().plusSeconds(3600),
        )
        return User(
            id = id,
            userInfo = userInfo,
            tokenInfo = tokenInfo,
        )
    }

    private fun createMemberWithParts(
        email: String,
        partNames: Set<String>,
    ): Member {
        val division = Division(
            id = 1L,
            name = "Division",
            sortPriority = 1,
        )
        val parts: java.util.SortedSet<Part> = partNames.mapIndexed { index, name ->
            Part(
                id = index.toLong() + 1,
                division = division,
                name = name,
                sortPriority = index,
            )
        }.toSortedSet()
        val department = Department(
            id = 1L,
            collegeId = 1L,
            name = "컴퓨터학부",
        )
        return Member(
            id = 10L,
            name = "홍길동",
            email = email,
            phoneNumber = "010-0000-0000",
            birthDate = LocalDate.of(2000, 1, 1),
            department = department,
            studentId = "20210001",
            parts = parts,
            role = MemberRole.MEMBER,
            nicknameEnglish = "roro",
            nicknameKorean = "로로",
            state = com.yourssu.scouter.hrms.implement.domain.member.MemberState.ACTIVE,
            joinDate = LocalDate.of(2020, 3, 1),
            note = "",
            stateUpdatedTime = Instant.now(),
        )
    }
}

