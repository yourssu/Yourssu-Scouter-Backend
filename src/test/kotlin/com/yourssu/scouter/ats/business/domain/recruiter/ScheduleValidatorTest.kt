package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import com.yourssu.scouter.common.implement.domain.division.Division
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.Term
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.Year

@Suppress("NonAsciiCharacters")
class ScheduleValidatorTest {

    private val scheduleValidator = ScheduleValidator()

    private lateinit var part1: Part
    private lateinit var part2: Part

    companion object {
        private val STANDARD_INTERVIEW_TIME = LocalDateTime
            .of(2025, 12, 15, 14, 0)

        private val ALTERNATIVE_INTERVIEW_TIME = LocalDateTime
            .of(2025, 12, 15, 15, 0)
    }

    @BeforeEach
    fun setup() {
        val division = Division(1, "개발",1)

        part1 = Part(1, division, "백엔드", 1)
        part2 = Part(2, division, "프론트엔드", 1)

    }

    @Test
    fun `ScheduleValidator는 파트ID와 면접 시간이 같을 경우 DuplicateScheduleException을 반환한다`() {
        // given
        val applicant1 = createApplicant(part1)
        val applicant2 = createApplicant(part1)

        val schedules = listOf(
            Schedule(null, applicant1, STANDARD_INTERVIEW_TIME, part1),
            Schedule(null, applicant2, STANDARD_INTERVIEW_TIME, part1)
        )

        // when and then
        assertThatThrownBy {scheduleValidator.validateNoDuplicates(schedules)}
            .isInstanceOf(DuplicateScheduleException::class.java)
            .hasMessageContaining("중복된 면접 일정이 있습니다:")

    }

    @Test
    fun `ScheduleValidator는 다른 파트의 겹치는 시간은 예외를 발생시키지 않는다`() {
        // given
        val applicant1 = createApplicant(part1)
        val applicant2 = createApplicant(part2)

        val schedules = listOf(
            Schedule(null, applicant1, STANDARD_INTERVIEW_TIME, part1),
            Schedule(null, applicant2, STANDARD_INTERVIEW_TIME, part2)
        )
        // when and then
        scheduleValidator.validateNoDuplicates(schedules)
    }

    @Test
    fun `ScheduleValidator는 같은 파트의 다른 시간은 예외를 발생시키지 않는다`() {
        // given
        val applicant1 = createApplicant(part1)
        val applicant2 = createApplicant(part1)

        val schedules = listOf(
            Schedule(null, applicant1, STANDARD_INTERVIEW_TIME, part1),
            Schedule(null, applicant2, ALTERNATIVE_INTERVIEW_TIME, part1)
        )
        // when and then
        scheduleValidator.validateNoDuplicates(schedules)
    }

    private fun createApplicant(part: Part) = Applicant(
        null,
        "김철수",
        "test@example.com",
        "010-1234-5678",
        "22",
        "컴퓨터학부",
        "20210001",
        part,
        ApplicantState.UNDER_REVIEW,
        LocalDateTime.of(2025, 9, 24, 12, 30),
        Semester(1L, Year.of(2025), Term.SPRING),
        "2-2",
        emptyList()
    )
}