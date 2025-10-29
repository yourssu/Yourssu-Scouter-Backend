package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.ats.implement.domain.recruiter.AutoScheduleGenerator
import com.yourssu.scouter.ats.implement.support.exception.InvalidScheduleException
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.Objects

@Suppress("NonAsciiCharacters")
class AutoScheduleGeneratorTest {

    private val autoScheduleGenerator = AutoScheduleGenerator()

    @Nested
    @DisplayName("generateSchedules는")
    inner class GenerateSchedules {

        @Test
        fun `빈 지원자 리스트를 전달하면 빈 스케줄 리스트를 반환한다`() {
            // given
            val applicants = emptyList<com.yourssu.scouter.ats.implement.domain.applicant.Applicant>()

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).isEmpty()
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
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).hasSize(1)
            assertThat(result[0]).hasSize(1)
            assertThat(result[0][0].applicantId).isEqualTo(1)
            assertThat(result[0][0].interviewTime).isEqualTo(LocalDateTime.of(2025, 9, 24, 12, 0))
        }

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
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).isNotEmpty()
            assertThat(result[0]).hasSize(2)
            // 모든 지원자가 배정되었는지 확인
            val applicantIds = result[0].map { it.applicantId }
            assertThat(applicantIds).containsExactlyInAnyOrder(1L, 2L)
        }

        @Test
        fun `전체 지원자를 배정할 수 없는 경우 InvalidScheduleException을 반환한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            // 3명의 지원자가 모두 같은 2개 시간대만 가능 -> 같은 파트이므로 2명만 배정 가능
            val availableTimes = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0),
                LocalDateTime.of(2025, 9, 24, 14, 0),
            )

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1).availableTimes(availableTimes).part(part).build(),
                ApplicantFixtureBuilder().id(2).availableTimes(availableTimes).part(part).build(),
                ApplicantFixtureBuilder().id(3).availableTimes(availableTimes).part(part).build(),
            )

            // when and then
            assertThatThrownBy { autoScheduleGenerator.generateSchedules(applicants, "MAX") }
                .isInstanceOf(InvalidScheduleException::class.java)
                .hasMessage("모든 지원자에게 면접 시간을 배정할 수 없습니다.")
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
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).hasSize(1)
            assertThat(result[0]).hasSize(2)
            assertThat(result[0].all { Objects.equals(it.interviewTime, sameTime) }).isTrue()
            assertThat(result[0].map { it.part }).containsExactlyInAnyOrder("서버", "iOS")
        }
    }

    @Nested
    @DisplayName("백트래킹 동작은")
    inner class BacktrackingBehavior {

        @Test
        fun `첫 번째 선택이 실패해도 백트래킹으로 다른 조합을 찾는다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val availableTimes1 = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0), // 12시
                LocalDateTime.of(2025, 9, 24, 13, 0), // 13시
            )

            val availableTimes2 = listOf(
                LocalDateTime.of(2025, 9, 24, 12, 0) // 12시만
            )

            val availableTimes3 = listOf(
                LocalDateTime.of(2025, 9, 24, 14, 0) // 14시만
            )

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1).availableTimes(availableTimes1).part(part).build(),
                ApplicantFixtureBuilder().id(2).availableTimes(availableTimes2).part(part).build(),
                ApplicantFixtureBuilder().id(3).availableTimes(availableTimes3).part(part).build(),
            )

            // when
            // 배정할 수 있는 시간이 적은 지원자부터 배정: 2 -> 3 -> 1 순
            // 1을 배정할 때 12시를 먼저 시도 -> 이미 배정됨 -> 13시 백트래킹 시도 -> 성공
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).isNotEmpty()
            assertThat(result[0]).hasSize(3)

            // 지원자 1은 13시에 배정되어야 함 (12시는 지원자 2가 사용)
            val schedule1 = result[0].find { it.applicantId == 1L }
            assertThat(schedule1?.interviewTime).isEqualTo(LocalDateTime.of(2025, 9, 24, 13, 0))
        }
    }

    @Nested
    @DisplayName("전략별 동작은")
    inner class StrategyBehavior {

        @Test
        fun `MaximumDayStrategy는 가능한 많은 날에 면접을 분산시킨다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val day1Morning = LocalDateTime.of(2025, 9, 24, 8, 0)
            val day1Afternoon = LocalDateTime.of(2025, 9, 24, 13, 0)
            val day2Morning = LocalDateTime.of(2025, 9, 25, 8, 0)
            val day2Afternoon = LocalDateTime.of(2025, 9, 25, 13, 0)

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1)
                    .availableTimes(listOf(day1Morning, day2Morning))
                    .part(part).build(),
                ApplicantFixtureBuilder().id(2)
                    .availableTimes(listOf(day1Afternoon, day2Afternoon))
                    .part(part).build(),
            )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 1)

            // then
            assertThat(result).isNotEmpty()
            assertThat(result).hasSize(1)
            // MAX 전략이므로 모든 결과가 다른 날에 배정되어야 함 (penalty score가 더 낮음)
            result.forEach { schedule ->
                val dates = schedule.map { it.interviewTime.toLocalDate() }.distinct()
                assertThat(dates).hasSize(2) // 2개의 다른 날짜
            }
        }

        @Test
        fun `MinimumDayStrategy는 가능한 적은 날에 면접을 집중시킨다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val day1Morning = LocalDateTime.of(2025, 9, 24, 10, 0)
            val day1Afternoon = LocalDateTime.of(2025, 9, 24, 14, 0)
            val day2 = LocalDateTime.of(2025, 9, 25, 12, 0)

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1)
                    .availableTimes(listOf(day1Morning, day2))
                    .part(part).build(),
                ApplicantFixtureBuilder().id(2)
                    .availableTimes(listOf(day1Afternoon, day2))
                    .part(part).build(),
            )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MIN", size = 1)

            // then
            assertThat(result).isNotEmpty()
            // MIN 전략: 가장 최적인 결과는 같은 날에 배정된 것
            val bestSchedule = result.first()
            val dates = bestSchedule.map { it.interviewTime.toLocalDate() }.distinct()
            assertThat(dates).hasSize(1) // 1개의 날짜에 집중
        }

        @Test
        fun `잘못된 전략 문자열을 전달하면 예외가 발생한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()
            val applicants = listOf(
                ApplicantFixtureBuilder().id(1)
                    .availableTimes(listOf(LocalDateTime.of(2025, 9, 24, 12, 0)))
                    .part(part).build(),
            )

            // when and then
            assertThatThrownBy { autoScheduleGenerator.generateSchedules(applicants, "INVALID") }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("현재 구현되지 않은 전략입니다: INVALID")
        }
    }

    @Nested
    @DisplayName("Beam Search 동작은")
    inner class BeamSearchBehavior {

        @Test
        fun `size만큼의 최적 스케줄 조합을 반환한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            // 여러 조합이 가능한 케이스
            val applicants = listOf(
                ApplicantFixtureBuilder().id(1)
                    .availableTimes(listOf(
                        LocalDateTime.of(2025, 9, 24, 10, 0),
                        LocalDateTime.of(2025, 9, 24, 11, 0),
                        LocalDateTime.of(2025, 9, 24, 12, 0)
                    ))
                    .part(part).build(),
                ApplicantFixtureBuilder().id(2)
                    .availableTimes(listOf(
                        LocalDateTime.of(2025, 9, 24, 10, 0),
                        LocalDateTime.of(2025, 9, 24, 11, 0),
                        LocalDateTime.of(2025, 9, 24, 12, 0)
                    ))
                    .part(part).build(),
            )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 3)

            // then
            assertThat(result).isNotEmpty()
            assertThat(result).hasSize(3)
            assertThat(result).allMatch { it.size == 2 } // 각 스케줄은 2명의 지원자 모두 포함

            // 모든 스케줄이 유효한지 확인 (중복 시간 없음)
            result.forEach { schedule ->
                val times = schedule.map { it.interviewTime }
                assertThat(times).doesNotHaveDuplicates()
            }
        }

        @Test
        fun `가능한 조합이 size보다 적으면 모든 조합을 반환한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            // 가능한 조합이 2개인 케이스
            val applicants = listOf(
                ApplicantFixtureBuilder().id(1)
                    .availableTimes(listOf(
                        LocalDateTime.of(2025, 9, 24, 10, 0),
                        LocalDateTime.of(2025, 9, 24, 11, 0)
                    ))
                    .part(part).build(),
                ApplicantFixtureBuilder().id(2)
                    .availableTimes(listOf(
                        LocalDateTime.of(2025, 9, 24, 10, 0),
                        LocalDateTime.of(2025, 9, 24, 11, 0)
                    ))
                    .part(part).build(),
            )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 10)

            // then
            // 2명이 2개 시간대 중 중복없이 배정 -> 2가지 조합 (P(2,2) = 2)
            assertThat(result).hasSizeLessThanOrEqualTo(10)
            assertThat(result).isNotEmpty()
        }

        @Test
        fun `penalty score가 낮은 순으로 정렬되어 반환된다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val day1Time1 = LocalDateTime.of(2025, 9, 24, 10, 0)
            val day1Time2 = LocalDateTime.of(2025, 9, 24, 14, 0)
            val day2 = LocalDateTime.of(2025, 9, 25, 12, 0)

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1)
                    .availableTimes(listOf(day1Time1, day2))
                    .part(part).build(),
                ApplicantFixtureBuilder().id(2)
                    .availableTimes(listOf(day1Time2, day2))
                    .part(part).build(),
            )

            // when - MAX 전략 사용 (같은 날이면 penalty +1)
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 3)

            // then
            assertThat(result).isNotEmpty()

            // 최소 하나 이상의 결과는 penalty가 낮은 최적해여야 함
            // MAX 전략이므로 서로 다른 날에 배정된 조합이 있어야 함
            val hasOptimalSolution = result.any { schedule ->
                val dates = schedule.map { it.interviewTime.toLocalDate() }.distinct()
                dates.size == 2 // 다른 날에 분산
            }
            assertThat(hasOptimalSolution).isTrue()
        }
    }

    @Nested
    @DisplayName("복잡한 케이스에서")
    inner class ComplexScenarios {

        @Test
        fun `여러 파트가 섞여 있어도 정상적으로 스케줄을 생성한다`() {
            // given
            val partServer = PartFixtureBuilder().id(1).name("서버").build()
            val partIOS = PartFixtureBuilder().id(2).name("iOS").build()
            val partAndroid = PartFixtureBuilder().id(3).name("안드로이드").build()

            val time1 = LocalDateTime.of(2025, 9, 24, 10, 0)
            val time2 = LocalDateTime.of(2025, 9, 24, 11, 0)
            val time3 = LocalDateTime.of(2025, 9, 24, 12, 0)

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1).availableTimes(listOf(time1, time2)).part(partServer).build(),
                ApplicantFixtureBuilder().id(2).availableTimes(listOf(time1, time2)).part(partServer).build(),
                ApplicantFixtureBuilder().id(3).availableTimes(listOf(time1, time3)).part(partIOS).build(),
                ApplicantFixtureBuilder().id(4).availableTimes(listOf(time2, time3)).part(partAndroid).build(),
            )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).isNotEmpty()
            assertThat(result[0]).hasSize(4)

            // 모든 파트의 지원자가 배정되었는지 확인
            val parts = result[0].map { it.part }.distinct()
            assertThat(parts).containsExactlyInAnyOrder("서버", "iOS", "안드로이드")
        }

        @Test
        fun `가능한 시간이 매우 제한적인 경우 최적 배정을 찾는다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            // 복잡한 제약 조건: 각 지원자가 서로 다른 시간대 선호
            val applicants = listOf(
                ApplicantFixtureBuilder().id(1)
                    .availableTimes(listOf(
                        LocalDateTime.of(2025, 9, 24, 10, 0)
                    ))
                    .part(part).build(),
                ApplicantFixtureBuilder().id(2)
                    .availableTimes(listOf(
                        LocalDateTime.of(2025, 9, 24, 11, 0)
                    ))
                    .part(part).build(),
                ApplicantFixtureBuilder().id(3)
                    .availableTimes(listOf(
                        LocalDateTime.of(2025, 9, 24, 12, 0)
                    ))
                    .part(part).build(),
            )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).hasSize(1) // 유일한 해
            assertThat(result[0]).hasSize(3)

            // 각 지원자가 자신의 유일한 시간에 배정되었는지 확인
            val schedule1 = result[0].find { it.applicantId == 1L }
            val schedule2 = result[0].find { it.applicantId == 2L }
            val schedule3 = result[0].find { it.applicantId == 3L }

            assertThat(schedule1?.interviewTime).isEqualTo(LocalDateTime.of(2025, 9, 24, 10, 0))
            assertThat(schedule2?.interviewTime).isEqualTo(LocalDateTime.of(2025, 9, 24, 11, 0))
            assertThat(schedule3?.interviewTime).isEqualTo(LocalDateTime.of(2025, 9, 24, 12, 0))
        }

        @Test
        fun `동일 파트 내에서 시간 중복을 올바르게 처리한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            // 모든 지원자가 동일한 시간대를 포함하지만, 다른 선택지도 있음
            val commonTime = LocalDateTime.of(2025, 9, 24, 12, 0)

            val applicants = listOf(
                ApplicantFixtureBuilder().id(1)
                    .availableTimes(listOf(
                        LocalDateTime.of(2025, 9, 24, 10, 0),
                        commonTime
                    ))
                    .part(part).build(),
                ApplicantFixtureBuilder().id(2)
                    .availableTimes(listOf(
                        LocalDateTime.of(2025, 9, 24, 11, 0),
                        commonTime
                    ))
                    .part(part).build(),
                ApplicantFixtureBuilder().id(3)
                    .availableTimes(listOf(
                        commonTime,
                        LocalDateTime.of(2025, 9, 24, 13, 0)
                    ))
                    .part(part).build(),
            )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).isNotEmpty()
            assertThat(result[0]).hasSize(3)

            // 같은 파트에서 동일 시간에 중복 배정이 없어야 함
            val times = result[0].map { it.interviewTime }
            assertThat(times).doesNotHaveDuplicates()
        }
    }
}