package com.yourssu.scouter.member.core.application

import com.yourssu.scouter.member.core.application.dto.ReadActiveMemberResponse

import com.yourssu.scouter.member.core.application.dto.ReadCompletedMemberResponse

import com.yourssu.scouter.member.core.application.dto.ReadGraduatedMemberResponse

import com.yourssu.scouter.member.core.application.dto.ReadWithdrawnMemberResponse

import com.yourssu.scouter.member.core.application.dto.ReadInactiveMemberResponse

import com.yourssu.scouter.member.core.business.dto.ActiveMemberDto
import com.yourssu.scouter.member.core.business.dto.CompletedMemberDto
import com.yourssu.scouter.member.core.business.dto.GraduatedMemberDto
import com.yourssu.scouter.member.core.business.dto.InactiveMemberDto
import com.yourssu.scouter.member.core.business.dto.MemberDto
import com.yourssu.scouter.member.core.business.dto.SemesterPeriodDto
import com.yourssu.scouter.member.core.business.dto.WithdrawnMemberDto
import com.yourssu.scouter.masterdata.department.business.dto.DepartmentDto
import com.yourssu.scouter.masterdata.division.business.dto.DivisionDto
import com.yourssu.scouter.masterdata.part.business.dto.PartDto
import com.yourssu.scouter.masterdata.semester.business.dto.SemesterDto
import com.yourssu.scouter.masterdata.semester.implement.Term
import com.yourssu.scouter.member.core.implement.MemberRole
import com.yourssu.scouter.member.core.implement.MemberState
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.Year

class MemberResponseMaskingTest {

    @Test
    fun `ReadActiveMemberResponse 마스킹 시 민감필드는 null, isSensitiveMasked는 true가 된다`() {
        val dto = createMemberDto()
        val activeDto = ActiveMemberDto(
            id = 1L,
            member = dto,
            isMembershipFeePaid = true,
        )
        val response = ReadActiveMemberResponse.from(activeDto)

        val masked = response.maskSensitive()

        assertThat(masked.phoneNumber).isNull()
        assertThat(masked.studentId).isNull()
        assertThat(masked.birthDate).isNull()
        assertThat(masked.membershipFee).isNull()
        assertThat(masked.note).isNull()
        assertThat(masked.isSensitiveMasked).isTrue()

        // 비민감 필드는 유지
        assertThat(masked.memberId).isEqualTo(response.memberId)
        assertThat(masked.email).isEqualTo(response.email)
        assertThat(masked.department).isEqualTo(response.department)
    }

    @Test
    fun `ReadInactiveMemberResponse 마스킹 시 민감필드는 null, isSensitiveMasked는 true가 된다`() {
        val dto = createMemberDto()
        val activeStartDto = SemesterDto(
            id = 1L,
            year = Year.of(2023),
            term = Term.SPRING,
        )
        val activeEndDto = SemesterDto(
            id = 2L,
            year = Year.of(2023),
            term = Term.FALL,
        )
        val inactiveStartDto = SemesterDto(
            id = 3L,
            year = Year.of(2024),
            term = Term.SPRING,
        )
        val inactiveEndDto = SemesterDto(
            id = 4L,
            year = Year.of(2024),
            term = Term.FALL,
        )
        val expectedReturnSemesterDto = SemesterDto(
            id = 5L,
            year = Year.of(2024),
            term = Term.SPRING,
        )

        val inactiveDto = InactiveMemberDto(
            id = 1L,
            member = dto,
            activePeriod = SemesterPeriodDto(
                startSemester = activeStartDto,
                endSemester = activeEndDto,
            ),
            expectedReturnSemester = expectedReturnSemesterDto,
            inactivePeriod = SemesterPeriodDto(
                startSemester = inactiveStartDto,
                endSemester = inactiveEndDto,
            ),
            activitySemestersLabel = "23-1, 24-2~25-1",
            totalActiveSemesters = 3,
        )
        val response = ReadInactiveMemberResponse.from(inactiveDto)

        val masked = response.maskSensitive()

        assertThat(masked.phoneNumber).isNull()
        assertThat(masked.studentId).isNull()
        assertThat(masked.birthDate).isNull()
        assertThat(masked.expectedReturnSemester).isNull()
        assertThat(masked.activitySemestersLabel).isNull()
        assertThat(masked.totalActiveSemesters).isNull()
        assertThat(masked.note).isNull()
        assertThat(masked.isSensitiveMasked).isTrue()

        assertThat(masked.memberId).isEqualTo(response.memberId)
        assertThat(masked.email).isEqualTo(response.email)
        assertThat(masked.department).isEqualTo(response.department)
    }

    @Test
    fun `ReadCompletedMemberResponse 마스킹 시 민감필드는 null, isSensitiveMasked는 true가 된다`() {
        val dto = createMemberDto()
        val completionSemesterDto = SemesterDto(
            id = 20L,
            year = Year.of(2025),
            term = Term.SPRING,
        )
        val completedDto = CompletedMemberDto(
            id = 1L,
            member = dto,
            completionSemester = completionSemesterDto,
        )
        val response = ReadCompletedMemberResponse.from(completedDto)

        val masked = response.maskSensitive()

        assertThat(masked.phoneNumber).isNull()
        assertThat(masked.studentId).isNull()
        assertThat(masked.birthDate).isNull()
        assertThat(masked.completionSemester).isNull()
        assertThat(masked.note).isNull()
        assertThat(masked.isSensitiveMasked).isTrue()

        assertThat(masked.memberId).isEqualTo(response.memberId)
        assertThat(masked.email).isEqualTo(response.email)
        assertThat(masked.department).isEqualTo(response.department)
    }

    @Test
    fun `ReadGraduatedMemberResponse 마스킹 시 민감필드는 null, isSensitiveMasked는 true가 된다`() {
        val dto = createMemberDto()
        val graduatedActiveStartDto = SemesterDto(
            id = 10L,
            year = Year.of(2021),
            term = Term.SPRING,
        )
        val graduatedActiveEndDto = SemesterDto(
            id = 11L,
            year = Year.of(2022),
            term = Term.SPRING,
        )

        val graduatedDto = GraduatedMemberDto(
            id = 1L,
            member = dto,
            activePeriod = SemesterPeriodDto(
                startSemester = graduatedActiveStartDto,
                endSemester = graduatedActiveEndDto,
            ),
            isAdvisorDesired = true,
        )
        val response = ReadGraduatedMemberResponse.from(graduatedDto)

        val masked = response.maskSensitive()

        assertThat(masked.phoneNumber).isNull()
        assertThat(masked.studentId).isNull()
        assertThat(masked.birthDate).isNull()
        assertThat(masked.note).isNull()
        assertThat(masked.isSensitiveMasked).isTrue()

        assertThat(masked.memberId).isEqualTo(response.memberId)
        assertThat(masked.email).isEqualTo(response.email)
        assertThat(masked.department).isEqualTo(response.department)
    }

    @Test
    fun `ReadWithdrawnMemberResponse 마스킹 시 민감필드는 null, isSensitiveMasked는 true가 된다`() {
        val dto = createMemberDto()
        val withdrawnDto = WithdrawnMemberDto(
            id = 1L,
            member = dto,
            withdrawnDate = LocalDate.of(2024, 6, 1),
        )
        val response = ReadWithdrawnMemberResponse.from(withdrawnDto)

        val masked = response.maskSensitive()

        assertThat(masked.withdrawnDate).isNull()
        assertThat(masked.note).isNull()
        assertThat(masked.isSensitiveMasked).isTrue()

        assertThat(masked.memberId).isEqualTo(response.memberId)
        assertThat(masked.name).isEqualTo(response.name)
        assertThat(masked.nickname).isEqualTo(response.nickname)
    }

    private fun createMemberDto(): MemberDto {
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
        return MemberDto(
            id = 1L,
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
    }
}

