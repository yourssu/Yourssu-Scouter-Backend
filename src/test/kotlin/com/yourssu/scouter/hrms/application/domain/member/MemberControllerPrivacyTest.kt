package com.yourssu.scouter.hrms.application.domain.member

import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.hrms.business.domain.member.ActiveMemberDto
import com.yourssu.scouter.hrms.business.domain.member.MemberDto
import com.yourssu.scouter.hrms.business.domain.member.MemberPrivacyService
import com.yourssu.scouter.hrms.business.domain.member.MemberService
import com.yourssu.scouter.hrms.business.support.exception.MemberAccessDeniedException
import com.yourssu.scouter.common.business.domain.department.DepartmentDto
import com.yourssu.scouter.common.business.domain.division.DivisionDto
import com.yourssu.scouter.common.business.domain.part.PartDto
import com.yourssu.scouter.hrms.implement.domain.member.MemberRole
import com.yourssu.scouter.hrms.implement.domain.member.MemberState
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.LocalDate

class MemberControllerPrivacyTest {

    private val memberService: MemberService = mock()
    private val memberPrivacyService: MemberPrivacyService = mock()

    private val controller = MemberController(
        memberService = memberService,
        memberPrivacyService = memberPrivacyService,
    )

    @Test
    fun `readAllActive는 privileged가 아니면 민감 필드를 마스킹하고 상단 isSensitiveMasked를 true로 내려준다`() {
        // given
        val authUserInfo = AuthUserInfo(userId = 1L)
        whenever(memberPrivacyService.isPrivilegedUser(authUserInfo.userId)).thenReturn(false)

        val dto = createActiveMemberDto()
        whenever(memberService.readAllActiveByFilters(search = null, partId = null))
            .thenReturn(listOf(dto))

        // when
        val responseEntity = controller.readAllActive(
            authUserInfo = authUserInfo,
            search = null,
            partId = null,
        )
        val body = responseEntity.body!!

        // then
        assertThat(body.isSensitiveMasked).isTrue()
        assertThat(body.members).hasSize(1)
        val first = body.members.first()
        assertThat(first.phoneNumber).isNull()
        assertThat(first.studentId).isNull()
        assertThat(first.birthDate).isNull()
        assertThat(first.membershipFee).isNull()
        assertThat(first.note).isNull()

        // 비민감 필드는 그대로
        assertThat(first.email).isEqualTo(dto.member.email)
        assertThat(first.department).isEqualTo(dto.member.department.name)
    }

    @Test
    fun `readAllActive는 privileged이면 민감 필드를 마스킹하지 않고 isSensitiveMasked를 false로 내려준다`() {
        // given
        val authUserInfo = AuthUserInfo(userId = 2L)
        whenever(memberPrivacyService.isPrivilegedUser(authUserInfo.userId)).thenReturn(true)

        val dto = createActiveMemberDto()
        whenever(memberService.readAllActiveByFilters(search = "홍", partId = 1L))
            .thenReturn(listOf(dto))

        // when
        val responseEntity = controller.readAllActive(
            authUserInfo = authUserInfo,
            search = "홍",
            partId = 1L,
        )
        val body = responseEntity.body!!

        // then
        assertThat(body.isSensitiveMasked).isFalse()
        assertThat(body.members).hasSize(1)
        val first = body.members.first()
        assertThat(first.phoneNumber).isEqualTo(dto.member.phoneNumber)
        assertThat(first.studentId).isEqualTo(dto.member.studentId)
        assertThat(first.birthDate).isEqualTo(dto.member.birthDate)
        assertThat(first.membershipFee).isTrue()
        assertThat(first.note).isEqualTo(dto.member.note)
    }

    @Test
    fun `updateActiveById는 privileged가 아니면 MemberAccessDeniedException을 던진다`() {
        // given
        val authUserInfo = AuthUserInfo(userId = 3L)
        whenever(memberPrivacyService.isPrivilegedUser(authUserInfo.userId)).thenReturn(false)

        val request = UpdateActiveMemberRequest(
            name = "새 이름",
        )

        // when & then
        assertThatThrownBy {
            controller.updateActiveById(
                authUserInfo = authUserInfo,
                memberId = 10L,
                request = request,
            )
        }.isInstanceOf(MemberAccessDeniedException::class.java)

        verify(memberService, org.mockito.kotlin.never()).updateActiveById(any())
    }

    @Test
    fun `updateInactiveById는 privileged가 아니면 MemberAccessDeniedException을 던진다`() {
        // given
        val authUserInfo = AuthUserInfo(userId = 4L)
        whenever(memberPrivacyService.isPrivilegedUser(authUserInfo.userId)).thenReturn(false)

        val request = UpdateInactiveMemberRequest(
            name = "새 이름",
        )

        // when & then
        assertThatThrownBy {
            controller.updateInactiveById(
                authUserInfo = authUserInfo,
                memberId = 11L,
                request = request,
            )
        }.isInstanceOf(MemberAccessDeniedException::class.java)

        verify(memberService, org.mockito.kotlin.never()).updateInactiveById(any())
    }

    @Test
    fun `updateGraduatedById는 privileged가 아니면 MemberAccessDeniedException을 던진다`() {
        // given
        val authUserInfo = AuthUserInfo(userId = 5L)
        whenever(memberPrivacyService.isPrivilegedUser(authUserInfo.userId)).thenReturn(false)

        val request = UpdateGraduatedMemberRequest(
            name = "새 이름",
        )

        // when & then
        assertThatThrownBy {
            controller.updateGraduatedById(
                authUserInfo = authUserInfo,
                memberId = 12L,
                request = request,
            )
        }.isInstanceOf(MemberAccessDeniedException::class.java)

        verify(memberService, org.mockito.kotlin.never()).updateGraduatedById(any())
    }

    @Test
    fun `updateWithdrawnById는 privileged가 아니면 MemberAccessDeniedException을 던진다`() {
        // given
        val authUserInfo = AuthUserInfo(userId = 6L)
        whenever(memberPrivacyService.isPrivilegedUser(authUserInfo.userId)).thenReturn(false)

        val request = UpdateWithdrawnMemberRequest(
            name = "새 이름",
        )

        // when & then
        assertThatThrownBy {
            controller.updateWithdrawnById(
                authUserInfo = authUserInfo,
                memberId = 13L,
                request = request,
            )
        }.isInstanceOf(MemberAccessDeniedException::class.java)

        verify(memberService, org.mockito.kotlin.never()).updateWithdrawnById(any())
    }

    private fun createActiveMemberDto(): ActiveMemberDto {
        val divisionDto = DivisionDto(
            id = 1L,
            name = "Division",
        )
        val partDto = PartDto(
            id = 1L,
            division = divisionDto,
            name = "Backend",
        )
        val departmentDto = DepartmentDto(
            id = 1L,
            collegeId = 1L,
            name = "컴퓨터학부",
        )
        val now = Instant.now()
        val memberDto = MemberDto(
            id = 10L,
            name = "홍길동",
            email = "member@yourssu.com",
            phoneNumber = "010-1234-5678",
            birthDate = LocalDate.of(2000, 1, 1),
            department = departmentDto,
            studentId = "20210001",
            parts = listOf(partDto),
            role = MemberRole.MEMBER,
            nicknameEnglish = "roro",
            nicknameKorean = "로로",
            state = MemberState.ACTIVE,
            joinDate = LocalDate.of(2020, 3, 1),
            note = "비고",
            stateUpdatedTime = now,
            createdTime = now,
            updatedTime = now,
        )
        return ActiveMemberDto(
            id = 1L,
            member = memberDto,
            isMembershipFeePaid = true,
        )
    }
}

