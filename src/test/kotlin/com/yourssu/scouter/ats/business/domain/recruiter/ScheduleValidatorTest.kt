package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.common.implement.domain.part.Part
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

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
        part1 = PartFixtureBuilder().id(1).build()
        part2 = PartFixtureBuilder().id(2).build()
    }

    @Test
    fun `ScheduleValidator는 파트ID와 면접 시간이 같을 경우 DuplicateScheduleException을 반환한다`() {
        // given
        val applicant1 = ApplicantFixtureBuilder().part(part1).build()
        val applicant2 = ApplicantFixtureBuilder().part(part1).build()

        val schedules = listOf(
            Schedule(null, applicant1, STANDARD_INTERVIEW_TIME, part1),
            Schedule(null, applicant2, STANDARD_INTERVIEW_TIME, part1)
        )

        // when and then
        assertThatThrownBy { scheduleValidator.validateNoDuplicates(schedules) }
            .isInstanceOf(DuplicateScheduleException::class.java)
            .hasMessageContaining("중복된 면접 일정이 있습니다:")

    }

    @Test
    fun `ScheduleValidator는 다른 파트의 겹치는 시간은 예외를 발생시키지 않는다`() {
        // given
        val applicant1 = ApplicantFixtureBuilder().part(part1).build()
        val applicant2 = ApplicantFixtureBuilder().part(part2).build()

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
        val applicant1 = ApplicantFixtureBuilder().part(part1).build()
        val applicant2 = ApplicantFixtureBuilder().part(part1).build()

        val schedules = listOf(
            Schedule(null, applicant1, STANDARD_INTERVIEW_TIME, part1),
            Schedule(null, applicant2, ALTERNATIVE_INTERVIEW_TIME, part1)
        )
        // when and then
        scheduleValidator.validateNoDuplicates(schedules)
    }

}