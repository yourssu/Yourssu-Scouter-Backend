package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.business.domain.division.DivisionDto
import com.yourssu.scouter.common.business.domain.part.PartDto
import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.common.implement.domain.semester.Term
import com.yourssu.scouter.hrms.business.domain.member.MemberPrivacyService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.Year

@Suppress("NonAsciiCharacters")
class ApplicantPrivacyServiceTest {

    private val memberPrivacyService: MemberPrivacyService = mock()

    private val service = ApplicantPrivacyService(
        memberPrivacyService = memberPrivacyService,
    )

    @Nested
    @DisplayName("filterAccessibleApplicants 호출 시")
    inner class FilterAccessibleApplicantsTest {

        @Test
        fun `privileged 유저이면 모든 지원자가 반환된다`() {
            // given
            val userId = 1L
            val dtos = listOf(
                createApplicantDto(partId = 10L),
                createApplicantDto(partId = 20L),
            )
            whenever(memberPrivacyService.isPrivilegedUser(userId)).thenReturn(true)

            // when
            val result = service.filterAccessibleApplicants(userId, dtos)

            // then
            assertThat(result).hasSize(2)
        }

        @Test
        fun `privileged가 아니면 같은 파트의 지원자만 반환된다`() {
            // given
            val userId = 2L
            val dtos = listOf(
                createApplicantDto(partId = 10L, name = "같은파트"),
                createApplicantDto(partId = 20L, name = "다른파트"),
            )
            whenever(memberPrivacyService.isPrivilegedUser(userId)).thenReturn(false)
            whenever(memberPrivacyService.getMemberPartIds(userId)).thenReturn(setOf(10L))

            // when
            val result = service.filterAccessibleApplicants(userId, dtos)

            // then
            assertThat(result).hasSize(1)
            assertThat(result.first().name).isEqualTo("같은파트")
        }

        @Test
        fun `멤버가 아닌 유저는 빈 목록이 반환된다`() {
            // given
            val userId = 3L
            val dtos = listOf(createApplicantDto(partId = 10L))
            whenever(memberPrivacyService.isPrivilegedUser(userId)).thenReturn(false)
            whenever(memberPrivacyService.getMemberPartIds(userId)).thenReturn(emptySet())

            // when
            val result = service.filterAccessibleApplicants(userId, dtos)

            // then
            assertThat(result).isEmpty()
        }

        @Test
        fun `빈 목록이 입력되면 빈 목록이 반환된다`() {
            // given
            val userId = 1L
            whenever(memberPrivacyService.isPrivilegedUser(userId)).thenReturn(false)
            whenever(memberPrivacyService.getMemberPartIds(userId)).thenReturn(setOf(10L))

            // when
            val result = service.filterAccessibleApplicants(userId, emptyList())

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
            applicationDateTime = Instant.parse("2025-03-01T00:00:00Z"),
            applicationSemester = semesterDto,
            academicSemester = "4학기",
            availableTimes = listOf(Instant.parse("2025-03-10T09:00:00Z")),
        )
    }
}
