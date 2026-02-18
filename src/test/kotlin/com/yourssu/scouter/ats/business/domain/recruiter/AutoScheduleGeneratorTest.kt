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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.system.measureTimeMillis

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
            val availableTimes =
                listOf(
                    Instant.parse("2025-09-24T10:00:00.00Z"),
                )
            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1).availableTimes(availableTimes).part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).hasSize(1)
            assertThat(result[0]).hasSize(1)
            assertThat(result[0][0].applicantId).isEqualTo(1)
            assertThat(result[0][0].startTime).isEqualTo(Instant.parse("2025-09-24T10:00:00Z"))
        }

        @Test
        fun `가능한 케이스가 있는 지원자 목록을 이용해 면접 스케줄을 생성한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val availableTimes1 =
                listOf(
                    Instant.parse("2025-09-24T12:00:00Z"),
                    Instant.parse("2025-09-24T13:00:00Z"),
                    Instant.parse("2025-09-24T14:00:00Z"),
                )

            val availableTimes2 =
                listOf(
                    Instant.parse("2025-09-24T12:00:00Z"),
                    Instant.parse("2025-09-24T13:00:00Z"),
                )

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1).availableTimes(availableTimes1).part(part).build(),
                    ApplicantFixtureBuilder().id(2).availableTimes(availableTimes2).part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).isNotEmpty()
            assertThat(result[0]).hasSize(2)
            val applicantIds = result[0].map { it.applicantId }
            assertThat(applicantIds).containsExactlyInAnyOrder(1L, 2L)
        }

        @Test
        fun `전체 지원자를 배정할 수 없는 경우 InvalidScheduleException을 반환한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val availableTimes =
                listOf(
                    Instant.parse("2025-09-24T12:00:00Z"),
                    Instant.parse("2025-09-24T14:00:00Z"),
                )

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1).availableTimes(availableTimes).part(part).build(),
                    ApplicantFixtureBuilder().id(2).availableTimes(availableTimes).part(part).build(),
                    ApplicantFixtureBuilder().id(3).availableTimes(availableTimes).part(part).build(),
                )

            // when & then
            assertThatThrownBy { autoScheduleGenerator.generateSchedules(applicants, "MAX") }
                .isInstanceOf(InvalidScheduleException::class.java)
                .hasMessage("모든 지원자에게 면접 시간을 배정할 수 없습니다.")
        }
    }

    @Nested
    @DisplayName("백트래킹 동작은")
    inner class BacktrackingBehavior {
        @Test
        fun `첫 번째 선택이 실패해도 백트래킹으로 다른 조합을 찾는다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val availableTimes1 =
                listOf(
                    Instant.parse("2025-09-24T12:00:00Z"),
                    Instant.parse("2025-09-24T13:00:00Z"),
                )

            val availableTimes2 =
                listOf(
                    Instant.parse("2025-09-24T12:00:00Z"),
                )

            val availableTimes3 =
                listOf(
                    Instant.parse("2025-09-24T14:00:00Z"),
                )

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1).availableTimes(availableTimes1).part(part).build(),
                    ApplicantFixtureBuilder().id(2).availableTimes(availableTimes2).part(part).build(),
                    ApplicantFixtureBuilder().id(3).availableTimes(availableTimes3).part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).isNotEmpty()
            assertThat(result[0]).hasSize(3)

            val schedule1 = result[0].find { it.applicantId == 1L }
            assertThat(schedule1?.startTime).isEqualTo(Instant.parse("2025-09-24T13:00:00Z"))
        }
    }

    @Nested
    @DisplayName("전략별 동작은")
    inner class StrategyBehavior {
        @Test
        fun `MaximumDayStrategy는 가능한 많은 날에 면접을 분산시킨다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val day1Morning = Instant.parse("2025-09-24T08:00:00Z")
            val day1Afternoon = Instant.parse("2025-09-24T13:00:00Z")
            val day2Morning = Instant.parse("2025-09-25T08:00:00Z")
            val day2Afternoon = Instant.parse("2025-09-25T13:00:00Z")

            val applicants =
                listOf(
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
            result.forEach { schedule ->
                val dates = schedule.map { it.startTime }.distinct()
                assertThat(dates).hasSize(2)
            }
        }

        @Test
        fun `MinimumDayStrategy는 가능한 적은 날에 면접을 집중시킨다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val day1Morning =
                LocalDateTime.of(2025, 9, 24, 8, 0)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant()
            val day1Afternoon =
                LocalDateTime.of(2025, 9, 24, 14, 0)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant()
            val day2 =
                LocalDateTime.of(2025, 9, 25, 12, 0)
                    .atZone(ZoneId.of("Asia/Seoul"))
                    .toInstant()

            val applicants =
                listOf(
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
            val bestSchedule = result.first()
            val dates = bestSchedule.map { it.startTime.atZone(ZoneId.of("Asia/Seoul")).dayOfYear }.distinct()
            assertThat(dates).hasSize(1)
        }

        @Test
        fun `잘못된 전략 문자열을 전달하면 예외가 발생한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()
            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1)
                        .availableTimes(listOf(Instant.parse("2025-09-24T12:00:00Z")))
                        .part(part).build(),
                )

            // when & then
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

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                Instant.parse("2025-09-24T11:00:00Z"),
                                Instant.parse("2025-09-24T12:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(2)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                Instant.parse("2025-09-24T11:00:00Z"),
                                Instant.parse("2025-09-24T12:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 3)

            // then
            assertThat(result).isNotEmpty()
            assertThat(result).hasSize(3)
            assertThat(result).allMatch { it.size == 2 }

            result.forEach { schedule ->
                val times = schedule.map { it.startTime }
                assertThat(times).doesNotHaveDuplicates()
            }
        }

        @Test
        fun `가능한 조합이 size보다 적으면 모든 조합을 반환한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                Instant.parse("2025-09-24T11:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(2)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                Instant.parse("2025-09-24T11:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 10)

            // then
            assertThat(result).hasSizeLessThanOrEqualTo(10)
            assertThat(result).isNotEmpty()
        }

        @Test
        fun `penalty score가 낮은 순으로 정렬되어 반환된다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val day1Time1 = Instant.parse("2025-09-24T10:00:00Z")
            val day1Time2 = Instant.parse("2025-09-24T14:00:00Z")
            val day2 = Instant.parse("2025-09-25T12:00:00Z")

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1)
                        .availableTimes(listOf(day1Time1, day2))
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(2)
                        .availableTimes(listOf(day1Time2, day2))
                        .part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 3)

            // then
            assertThat(result).isNotEmpty()

            val hasOptimalSolution =
                result.any { schedule ->
                    val dates = schedule.map { it.startTime }.distinct()
                    dates.size == 2
                }
            assertThat(hasOptimalSolution).isTrue()
        }

        @Test
        fun `size가 가능한 조합보다 클 때 중복된 스케줄을 생성하지 않는다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                Instant.parse("2025-09-24T11:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(2)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                Instant.parse("2025-09-24T11:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 10)

            // then
            assertThat(result).hasSizeLessThanOrEqualTo(2)

            val signatures =
                result.map { schedule ->
                    schedule
                        .sortedWith(compareBy({ it.applicantId }, { it.startTime }))
                        .joinToString("|") { "${it.applicantId}:${it.startTime}" }
                }
            assertThat(signatures).doesNotHaveDuplicates()
        }

        @Test
        fun `모든 반환된 스케줄 조합이 고유하다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                Instant.parse("2025-09-24T11:00:00Z"),
                                Instant.parse("2025-09-24T12:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(2)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                Instant.parse("2025-09-24T11:00:00Z"),
                                Instant.parse("2025-09-24T12:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(3)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                Instant.parse("2025-09-24T11:00:00Z"),
                                Instant.parse("2025-09-24T12:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 5)

            // then
            assertThat(result).isNotEmpty()

            val signatures =
                result.map { schedule ->
                    schedule
                        .sortedWith(compareBy({ it.applicantId }, { it.startTime }))
                        .joinToString("|") { "${it.applicantId}:${it.startTime}" }
                }

            assertThat(signatures).doesNotHaveDuplicates()
            assertThat(signatures).hasSize(result.size)
        }

        @Test
        fun `단일 조합만 가능한 경우 size와 관계없이 1개만 반환한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1)
                        .availableTimes(listOf(Instant.parse("2025-09-24T10:00:00Z")))
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(2)
                        .availableTimes(listOf(Instant.parse("2025-09-24T11:00:00Z")))
                        .part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 100)

            // then
            assertThat(result).hasSize(1)
            assertThat(result[0]).hasSize(2)
        }
    }

    @Nested
    @DisplayName("복잡한 케이스에서")
    inner class ComplexScenarios {
        @Test
        fun `가능한 시간이 매우 제한적인 경우 최적 배정을 찾는다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1)
                        .availableTimes(listOf(Instant.parse("2025-09-24T10:00:00Z")))
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(2)
                        .availableTimes(listOf(Instant.parse("2025-09-24T11:00:00Z")))
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(3)
                        .availableTimes(listOf(Instant.parse("2025-09-24T12:00:00Z")))
                        .part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).hasSize(1)
            assertThat(result[0]).hasSize(3)

            val schedule1 = result[0].find { it.applicantId == 1L }
            val schedule2 = result[0].find { it.applicantId == 2L }
            val schedule3 = result[0].find { it.applicantId == 3L }

            assertThat(schedule1?.startTime).isEqualTo(Instant.parse("2025-09-24T10:00:00Z"))
            assertThat(schedule2?.startTime).isEqualTo(Instant.parse("2025-09-24T11:00:00Z"))
            assertThat(schedule3?.startTime).isEqualTo(Instant.parse("2025-09-24T12:00:00Z"))
        }

        @Test
        fun `동일 파트 내에서 시간 중복을 올바르게 처리한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val commonTime = Instant.parse("2025-09-24T12:00:00Z")

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T10:00:00Z"),
                                commonTime,
                            ),
                        )
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(2)
                        .availableTimes(
                            listOf(
                                Instant.parse("2025-09-24T11:00:00Z"),
                                commonTime,
                            ),
                        )
                        .part(part).build(),
                    ApplicantFixtureBuilder().id(3)
                        .availableTimes(
                            listOf(
                                commonTime,
                                Instant.parse("2025-09-24T13:00:00Z"),
                            ),
                        )
                        .part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX")

            // then
            assertThat(result).isNotEmpty()
            assertThat(result[0]).hasSize(3)

            val times = result[0].map { it.startTime }
            assertThat(times).doesNotHaveDuplicates()
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    inner class PerformanceTest {
        @Test
        fun `5명의 지원자와 다양한 시간슬롯을 1초 내에 처리한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()
            val baseTime = Instant.parse("2025-09-24T09:00:00Z")

            val timeSlots = (0 until 10).map { baseTime.plusSeconds(it * 3600L) }

            val applicants =
                (1..5).map { id ->
                    ApplicantFixtureBuilder()
                        .id(id.toLong())
                        .availableTimes(timeSlots.take(5 + id))
                        .part(part)
                        .build()
                }

            // when
            val elapsedTime =
                measureTimeMillis {
                    val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 5)
                    assertThat(result).isNotEmpty()
                }

            // then
            assertThat(elapsedTime).isLessThan(1000L)
        }

        @Test
        fun `8명의 지원자와 고유한 시간슬롯을 1초 내에 처리한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()
            val baseTime = Instant.parse("2025-09-24T09:00:00Z")

            val applicants =
                (1..8).map { id ->
                    val personalSlots =
                        listOf(
                            baseTime.plusSeconds((id - 1) * 3600L),
                            baseTime.plusSeconds((id + 7) * 3600L),
                        )
                    ApplicantFixtureBuilder()
                        .id(id.toLong())
                        .availableTimes(personalSlots)
                        .part(part)
                        .build()
                }

            // when
            val elapsedTime =
                measureTimeMillis {
                    val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 3)
                    assertThat(result).isNotEmpty()
                }

            // then
            assertThat(elapsedTime).isLessThan(1000L)
        }
    }

    @Nested
    @DisplayName("최소 penalty 최적화는")
    inner class MinPenaltyOptimization {
        @Test
        fun `MAX 전략에서 고유 날짜가 충분하면 penalty 0인 결과를 반환한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val day1 = Instant.parse("2025-09-24T10:00:00Z")
            val day2 = Instant.parse("2025-09-25T10:00:00Z")
            val day3 = Instant.parse("2025-09-26T10:00:00Z")

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1).availableTimes(listOf(day1, day2)).part(part).build(),
                    ApplicantFixtureBuilder().id(2).availableTimes(listOf(day2, day3)).part(part).build(),
                    ApplicantFixtureBuilder().id(3).availableTimes(listOf(day1, day3)).part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 1)

            // then
            assertThat(result).hasSize(1)
            val dates = result[0].map { it.startTime }.distinct()
            assertThat(dates).hasSize(3)
        }

        @Test
        fun `MIN 전략에서 같은 날에 배정 가능하면 최적 결과를 반환한다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val day1Slot1 = Instant.parse("2025-09-24T10:00:00Z")
            val day1Slot2 = Instant.parse("2025-09-24T11:00:00Z")
            val day1Slot3 = Instant.parse("2025-09-24T12:00:00Z")

            val applicants =
                listOf(
                    ApplicantFixtureBuilder().id(1).availableTimes(listOf(day1Slot1, day1Slot2)).part(part).build(),
                    ApplicantFixtureBuilder().id(2).availableTimes(listOf(day1Slot2, day1Slot3)).part(part).build(),
                )

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MIN", size = 1)

            // then
            assertThat(result).hasSize(1)
            val dates =
                result[0].map {
                    it.startTime.atZone(java.time.ZoneId.of("Asia/Seoul")).toLocalDate()
                }.distinct()
            assertThat(dates).hasSize(1)
        }
    }

    @Nested
    @DisplayName("Greedy Fallback은")
    inner class GreedyFallback {
        @Test
        fun `모든 지원자에게 고유 슬롯이 있으면 greedy로 해결 가능하다`() {
            // given
            val part = PartFixtureBuilder().id(1).build()

            val applicants =
                (1..5).map { id ->
                    ApplicantFixtureBuilder()
                        .id(id.toLong())
                        .availableTimes(listOf(Instant.parse("2025-09-24T${9 + id}:00:00Z")))
                        .part(part)
                        .build()
                }

            // when
            val result = autoScheduleGenerator.generateSchedules(applicants, "MAX", size = 1)

            // then
            assertThat(result).hasSize(1)
            assertThat(result[0]).hasSize(5)
        }
    }
}
