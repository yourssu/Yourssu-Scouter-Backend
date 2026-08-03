package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantDto

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.implement.AssignmentResult
import com.yourssu.scouter.common.division.business.dto.DivisionDto
import com.yourssu.scouter.common.part.business.dto.PartDto
import com.yourssu.scouter.common.semester.business.dto.SemesterDto
import com.yourssu.scouter.common.semester.implement.Term
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.Year

@Suppress("NonAsciiCharacters")
class ApplicantPrivacyServiceTest {

    private val service = ApplicantPrivacyService()

    @Nested
    @DisplayName("filterAccessibleApplicants 호출 시")
    inner class FilterAccessibleApplicantsTest {

        @Test
        fun `privileged 유저이면 모든 지원자가 반환된다`() {
            // given
            val dtos = listOf(
                createApplicantDto(partId = 10L),
                createApplicantDto(partId = 20L),
            )
            val scope = ApplicantAccessScope(isPrivileged = true, memberPartIds = emptySet())

            // when
            val result = service.filterAccessibleApplicants(scope, dtos)

            // then
            assertThat(result).hasSize(2)
        }

        @Test
        fun `privileged가 아니면 같은 파트의 지원자만 반환된다`() {
            // given
            val dtos = listOf(
                createApplicantDto(partId = 10L, name = "같은파트"),
                createApplicantDto(partId = 20L, name = "다른파트"),
            )
            val scope = ApplicantAccessScope(isPrivileged = false, memberPartIds = setOf(10L))

            // when
            val result = service.filterAccessibleApplicants(scope, dtos)

            // then
            assertThat(result).hasSize(1)
            assertThat(result.first().name).isEqualTo("같은파트")
        }

        @Test
        fun `멤버가 아닌 유저는 빈 목록이 반환된다`() {
            // given
            val dtos = listOf(createApplicantDto(partId = 10L))
            val scope = ApplicantAccessScope(isPrivileged = false, memberPartIds = emptySet())

            // when
            val result = service.filterAccessibleApplicants(scope, dtos)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        fun `빈 목록이 입력되면 빈 목록이 반환된다`() {
            // given
            val scope = ApplicantAccessScope(isPrivileged = false, memberPartIds = setOf(10L))

            // when
            val result = service.filterAccessibleApplicants(scope, emptyList())

            // then
            assertThat(result).isEmpty()
        }
    }

    private fun createApplicantDto(partId: Long, name: String = "홍길동"): ApplicantDto {
        val divisionDto = DivisionDto(id = 1L, name = "개발")
        val partDto = PartDto(id = partId, division = divisionDto, name = "Server")
        val semesterDto = SemesterDto(id = 1L, year = Year.of(2025), term = Term.SPRING)

        return ApplicantDto(
            id = 1L,
            name = name,
            email = "test@example.com",
            phoneNumber = "010-1234-5678",
            age = "24",
            department = "컴퓨터공학과",
            studentId = "20201234",
            part = partDto,
            state = ApplicantState.UNDER_REVIEW,
            assignmentResult = AssignmentResult.NOT_SUBMITTED,
            applicationDateTime = Instant.parse("2025-03-01T00:00:00Z"),
            applicationSemester = semesterDto,
            academicSemester = "4학기",
            availableTimes = listOf(Instant.parse("2025-03-10T09:00:00Z")),
        )
    }
}
