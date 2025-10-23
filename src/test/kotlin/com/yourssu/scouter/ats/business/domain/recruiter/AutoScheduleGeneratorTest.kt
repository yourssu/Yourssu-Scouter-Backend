package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.ats.implement.support.exception.InvalidScheduleException
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@Suppress("NonAsciiCharacters")
class AutoScheduleGeneratorTest {

    private val autoScheduleGenerator = AutoScheduleGenerator()

    @Nested
    @DisplayName("generatedSchedules는")
    inner class GeneratedSchedulesTest {

        @Test
        fun `가능한 케이스가 있는 지원자 목록을 이용해 면접 스케줄을 생성한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val availableTimes1 = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0),
                LocalDateTime.of(2025, 9, 24, 13, 0),
                LocalDateTime.of(2025, 9, 24, 14, 0),
            )

            val availableTimes2 = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0),
                LocalDateTime.of(2025, 9, 24, 13, 0),
            )
            val applicants = listOf(
                ApplicantFixtureBuilder().id(1).availableTimes(availableTimes1).part(part).build(),
                ApplicantFixtureBuilder().id(2).availableTimes(availableTimes2).part(part).build(),
            )
            // when
            val generateSchedules = autoScheduleGenerator.generateSchedules(applicants)
            // then
            assertThat(generateSchedules).hasSize(2)
        }

        @Test
        fun `전체 지원자를 배정할 수 없는 경우 InvalidScheduleException을 반환한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val availableTimes1 = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0),
                LocalDateTime.of(2025, 9, 24, 14, 0),
            )

            val availableTimes2 = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0),
                LocalDateTime.of(2025, 9, 24, 14, 0),
            )

            val availableTimes3 = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0),
                LocalDateTime.of(2025, 9, 24, 14, 0),
            )

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1).availableTimes(availableTimes1).part(part).build(),
                ApplicantFixtureBuilder().id(2).availableTimes(availableTimes2).part(part).build(),
                ApplicantFixtureBuilder().id(3).availableTimes(availableTimes3).part(part).build(),
            )
            // when and then
            assertThatThrownBy { autoScheduleGenerator.generateSchedules(applicants) }
                .isInstanceOf(InvalidScheduleException::class.java)
                .hasMessage("모든 지원자에게 면접 시간을 배정할 수 없습니다.")
        }

        @Test
        fun `빈 지원자 리스트를 전달하면 빈 스케줄 리스트를 반환한다`() {
            // given
            val applicants = emptyList<com.yourssu.scouter.ats.implement.domain.applicant.Applicant>()

            // when
            val generateSchedules = autoScheduleGenerator.generateSchedules(applicants)

            // then
            assertThat(generateSchedules).isEmpty()
        }

        @Test
        fun `단일 지원자의 경우 정상적으로 스케줄을 생성한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()
            val availableTimes = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0),
            )
            val applicants = listOf(
                ApplicantFixtureBuilder().id(1).availableTimes(availableTimes).part(part).build(),
            )

            // when
            val generateSchedules = autoScheduleGenerator.generateSchedules(applicants)

            // then
            assertThat(generateSchedules).hasSize(1)
            assertThat(generateSchedules[0].applicantId).isEqualTo(1)
            assertThat(generateSchedules[0].interviewTime).isEqualTo(LocalDateTime.of(2025, 9, 24, 12, 0))
        }

        @Test
        fun `다른 파트의 지원자들은 같은 시간에 면접을 배정할 수 있다`() {
            // given
            val part1 = PartFixtureBuilder().id(1).name("서버").build()
            val part2 = PartFixtureBuilder().id(2).name("iOS").build()

            val sameTime = LocalDateTime.of(2025, 9, 24, 12, 0)
            val availableTimes = listOf(sameTime)

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1).availableTimes(availableTimes).part(part1).build(),
                ApplicantFixtureBuilder().id(2).availableTimes(availableTimes).part(part2).build(),
            )

            // when
            val generateSchedules = autoScheduleGenerator.generateSchedules(applicants)

            // then
            assertThat(generateSchedules).hasSize(2)
            assertThat(generateSchedules.all { it.interviewTime == sameTime }).isTrue()
            assertThat(generateSchedules.map { it.part }).containsExactlyInAnyOrder("서버", "iOS")
        }

        @Test
        fun `백트래킹이 제대로 동작하여 첫 번째 선택이 실패해도 다른 조합을 찾는다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val availableTimes1 = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0), // 12시
                LocalDateTime.of(2025, 9, 24, 13, 0), // 13시
            )

            val availableTimes2 = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0) // 12시
            )

            val availableTimes3 = listOf(
                LocalDateTime.of(2025, 9, 24, 14, 0) // 14시
            )

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1).availableTimes(availableTimes1).part(part).build(),
                ApplicantFixtureBuilder().id(2).availableTimes(availableTimes2).part(part).build(),
                ApplicantFixtureBuilder().id(3).availableTimes(availableTimes3).part(part).build(),
            )
            // when
            // 배정할 수 있는 시간이 적은 지원자 부터 배정함
            // 2 -> 3 -> 1 순으로 배정 될 것임.
            // 1을 배정할때 12시를 먼저 시도 -> 이미 배정되어 있음 -> 13시를 백트래킹으로 시도 -> 성공

            val schedules = autoScheduleGenerator.generateSchedules(applicants)
            // then
            assertThat(schedules).hasSize(3)
        }
    }
}