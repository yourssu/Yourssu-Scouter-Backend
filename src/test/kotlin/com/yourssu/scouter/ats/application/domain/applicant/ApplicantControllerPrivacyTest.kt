package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantDto
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantPrivacyService
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantService
import com.yourssu.scouter.ats.business.support.exception.ApplicantAccessDeniedException
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.division.DivisionDto
import com.yourssu.scouter.common.business.domain.part.PartDto
import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.common.implement.domain.semester.Term
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.Year

@Suppress("NonAsciiCharacters")
class ApplicantControllerPrivacyTest {

    private val applicantService: ApplicantService = mock()
    private val applicantPrivacyService: ApplicantPrivacyService = mock()

    private val controller = ApplicantController(
        applicantService = applicantService,
        applicantPrivacyService = applicantPrivacyService,
    )

    @Nested
    @DisplayName("readAll 호출 시")
    inner class ReadAllTest {

        @Test
        fun `필터링된 지원자만 반환된다`() {
            // given
            val authUserInfo = AuthUserInfo(userId = 1L)
            val accessibleDto = createApplicantDto(partId = 10L, name = "접근가능")
            val restrictedDto = createApplicantDto(partId = 20L, name = "접근불가")
            val allDtos = listOf(accessibleDto, restrictedDto)

            whenever(applicantService.readAllByFilters(name = null, state = null, semesterId = null, partId = null))
                .thenReturn(allDtos)
            whenever(applicantPrivacyService.filterAccessibleApplicants(eq(1L), eq(allDtos)))
                .thenReturn(listOf(accessibleDto))

            // when
            val responseEntity = controller.readAll(
                authUserInfo = authUserInfo,
                name = null,
                state = null,
                semesterId = null,
                partId = null,
            )
            val body = responseEntity.body!!

            // then
            assertThat(body).hasSize(1)
            assertThat(body.first().name).isEqualTo("접근가능")
        }

        @Test
        fun `권한 없는 유저는 빈 목록이 반환된다`() {
            // given
            val authUserInfo = AuthUserInfo(userId = 1L)
            val dto = createApplicantDto(partId = 10L)

            whenever(applicantService.readAllByFilters(name = null, state = null, semesterId = null, partId = null))
                .thenReturn(listOf(dto))
            whenever(applicantPrivacyService.filterAccessibleApplicants(eq(1L), any()))
                .thenReturn(emptyList())

            // when
            val responseEntity = controller.readAll(
                authUserInfo = authUserInfo,
                name = null,
                state = null,
                semesterId = null,
                partId = null,
            )
            val body = responseEntity.body!!

            // then
            assertThat(body).isEmpty()
        }
    }

    @Nested
    @DisplayName("readById 호출 시")
    inner class ReadByIdTest {

        @Test
        fun `권한 있으면 전체 정보가 반환된다`() {
            // given
            val authUserInfo = AuthUserInfo(userId = 1L)
            val dto = createApplicantDto(partId = 10L)
            whenever(applicantService.readById(1L)).thenReturn(dto)
            whenever(applicantPrivacyService.filterAccessibleApplicants(eq(1L), any()))
                .thenReturn(listOf(dto))

            // when
            val responseEntity = controller.readById(
                authUserInfo = authUserInfo,
                applicantId = 1L,
            )
            val body = responseEntity.body!!

            // then
            assertThat(body.email).isEqualTo(dto.email)
            assertThat(body.phoneNumber).isEqualTo(dto.phoneNumber)
            assertThat(body.studentId).isEqualTo(dto.studentId)
        }

        @Test
        fun `권한 없으면 ApplicantAccessDeniedException이 발생한다`() {
            // given
            val authUserInfo = AuthUserInfo(userId = 1L)
            val dto = createApplicantDto(partId = 10L)
            whenever(applicantService.readById(1L)).thenReturn(dto)
            whenever(applicantPrivacyService.filterAccessibleApplicants(eq(1L), any()))
                .thenReturn(emptyList())

            // when & then
            assertThatThrownBy {
                controller.readById(
                    authUserInfo = authUserInfo,
                    applicantId = 1L,
                )
            }.isInstanceOf(ApplicantAccessDeniedException::class.java)
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
