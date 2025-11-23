package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.applicant.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.ats.implement.domain.recruiter.*
import com.yourssu.scouter.ats.implement.support.exception.ApplicantNotFoundException
import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.support.exception.PartNotFoundException
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import java.time.Instant
import java.time.temporal.ChronoUnit

@Suppress("NonAsciiCharacters")
class ScheduleServiceTest {

    private lateinit var scheduleService: ScheduleService
    private lateinit var scheduleWriter: ScheduleWriter
    private lateinit var scheduleReader: ScheduleReader
    private lateinit var partReader: PartReader
    private lateinit var applicantReader: ApplicantReader
    private lateinit var scheduleValidator: ScheduleValidator

    private val futureTime = Instant.now().plus(7L, ChronoUnit.DAYS)

    @BeforeEach
    fun setUp() {
        scheduleWriter = mock(ScheduleWriter::class.java)
        scheduleReader = mock(ScheduleReader::class.java)
        partReader = mock(PartReader::class.java)
        applicantReader = mock(ApplicantReader::class.java)
        scheduleValidator = mock(ScheduleValidator::class.java)

        scheduleService = ScheduleService(
            scheduleWriter,
            scheduleReader,
            partReader,
            applicantReader,
            scheduleValidator,
            mock(AutoScheduleGenerator::class.java)
        )
    }

    @Nested
    @DisplayName("createSchedules 메서드는")
    inner class CreateSchedulesTests {

        @Test
        fun `정상적인 스케줄 생성 요청시 스케줄을 저장한다`() {
            // given
            val partId = 1L
            val applicantId = 100L
            val command = CreateScheduleCommand(
                applicantId = applicantId,
                startTime = futureTime,
                endTime = futureTime.plus(1, ChronoUnit.HOURS),
                partId = partId
            )

            val part = PartFixtureBuilder()
                .id(partId)
                .build()
            val applicant = ApplicantFixtureBuilder()
                .id(applicantId)
                .build()

            whenever(partReader.readAllByIds(listOf(partId))).thenReturn(listOf(part))
            whenever(applicantReader.readByIdsWithoutAvailableTimes(listOf(applicantId))).thenReturn(listOf(applicant))
            doNothing().whenever(scheduleValidator).validateNoDuplicates(any())
            doNothing().whenever(scheduleWriter).writeAll(any())

            // when
            scheduleService.createSchedules(listOf(command))

            // then
            val captor = argumentCaptor<List<Schedule>>()
            verify(scheduleWriter).writeAll(captor.capture())

            val savedSchedules = captor.firstValue
            assertThat(savedSchedules).hasSize(1)
            assertThat(savedSchedules[0].applicant.id).isEqualTo(applicantId)
            assertThat(savedSchedules[0].part.id).isEqualTo(partId)
            assertThat(savedSchedules[0].startTime).isEqualTo(futureTime)
        }

        @Test
        fun `중복된 스케줄 생성 요청시 예외가 발생한다`() {
            // given
            val partId = 1L
            val applicantId1 = 100L
            val applicantId2 = 101L
            val sameTime = futureTime

            val commands = listOf(
                CreateScheduleCommand(applicantId1, sameTime, sameTime.plus(1, ChronoUnit.HOURS), partId),
                CreateScheduleCommand(applicantId2, sameTime, sameTime.plus(1, ChronoUnit.HOURS), partId)
            )

            val part = PartFixtureBuilder().id(partId).build()
            val applicant1 = ApplicantFixtureBuilder()
                .id(applicantId1)
                .part(part)
                .build()
            val applicant2 = ApplicantFixtureBuilder()
                .id(applicantId2)
                .part(part)
                .build()

            whenever(partReader.readAllByIds(listOf(partId))).thenReturn(listOf(part))
            whenever(applicantReader.readByIdsWithoutAvailableTimes(listOf(applicantId1, applicantId2)))
                .thenReturn(listOf(applicant1, applicant2))

            doThrow(DuplicateScheduleException::class.java).whenever(scheduleValidator).validateNoDuplicates(any())

            // when and then
            assertThatThrownBy { scheduleService.createSchedules(commands) }
                .isInstanceOf(DuplicateScheduleException::class.java)
        }

        @Test
        fun `존재하지 않는 파트 ID로 스케줄 생성시 예외가 발생한다`() {
            // given
            val invalidPartId = 999L
            val applicantId = 100L
            val command = CreateScheduleCommand(applicantId, futureTime, futureTime.plus(1, ChronoUnit.HOURS), invalidPartId)

            whenever(partReader.readAllByIds(listOf(invalidPartId))).thenReturn(emptyList())
            whenever(applicantReader.readByIdsWithoutAvailableTimes(listOf(applicantId))).thenReturn(
                listOf(
                    ApplicantFixtureBuilder()
                        .id(applicantId)
                        .build()
                )
            )

            // when and then
            assertThatThrownBy {
                scheduleService.createSchedules(listOf(command))
            }.isInstanceOf(PartNotFoundException::class.java)
                .hasMessageContaining("파트를 찾을 수 없습니다")
        }

        @Test
        fun `존재하지 않는 지원자 ID로 스케줄 생성시 예외가 발생한다`() {
            // given
            val partId = 1L
            val invalidApplicantId = 999L
            val command = CreateScheduleCommand(invalidApplicantId, futureTime, futureTime.plus(1, ChronoUnit.HOURS), partId)

            val part = PartFixtureBuilder().id(partId).build()
            whenever(partReader.readAllByIds(listOf(partId))).thenReturn(listOf(part))
            whenever(applicantReader.readByIdsWithoutAvailableTimes(listOf(invalidApplicantId))).thenReturn(emptyList())

            // when and then
            assertThatThrownBy {
                scheduleService.createSchedules(listOf(command))
            }.isInstanceOf(ApplicantNotFoundException::class.java)
                .hasMessageContaining("지원자 정보를 찾을 수 없습니다")
        }
    }

    @Nested
    @DisplayName("readSchedules 메서드는")
    inner class ReadSchedulesTests {

        @Test
        fun `파트 ID가 null이면 전체 목록을 조회한다`() {
            // given
            val schedules = listOf(
                ReadScheduleDto(
                    id = 1L,
                    applicantId = 1L,
                    applicantName = "홍길동",
                    part = "백엔드",
                    startTime = futureTime,
                    endTime = futureTime.plus(1, ChronoUnit.HOURS)
                ),
                ReadScheduleDto(
                    id = 2L,
                    applicantId = 2L,
                    applicantName = "김철수",
                    part = "백엔드",
                    startTime = futureTime.plus(1, ChronoUnit.HOURS),
                    endTime = futureTime.plus(2, ChronoUnit.HOURS)
                )
            )

            whenever(scheduleReader.readAll()).thenReturn(schedules)

            // when
            val result = scheduleService.readSchedules(null)

            // then
            assertThat(result).hasSize(2)
            assertThat(result[0].name).isEqualTo("홍길동")
            assertThat(result[1].name).isEqualTo("김철수")
            assertThat(result).allMatch { it.part == "백엔드" }
        }

        @Test
        fun `파트 ID로 스케줄 목록을 조회한다`() {
            // given
            val partId = 1L
            val schedules = listOf(
                ReadScheduleDto(
                    id = 1L,
                    applicantId = 1L,
                    applicantName = "홍길동",
                    part = "백엔드",
                    startTime = futureTime,
                    endTime = futureTime.plus(1, ChronoUnit.HOURS)
                ),
                ReadScheduleDto(
                    id = 2L,
                    applicantId = 2L,
                    applicantName = "김철수",
                    part = "백엔드",
                    startTime = futureTime.plus(1, ChronoUnit.HOURS),
                    endTime = futureTime.plus(2, ChronoUnit.HOURS)
                )
            )

            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(schedules)

            // when
            val result = scheduleService.readSchedules(partId)

            // then
            assertThat(result).hasSize(2)
            assertThat(result[0].name).isEqualTo("홍길동")
            assertThat(result[1].name).isEqualTo("김철수")
            assertThat(result).allMatch { it.part == "백엔드" }
        }

        @Test
        fun `해당 파트에 스케줄이 없으면 빈 리스트를 반환한다`() {
            // given
            val partId = 1L
            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(emptyList())

            // when
            val result = scheduleService.readSchedules(partId)

            // then
            assertThat(result).isEmpty()
        }
    }

    @Nested
    @DisplayName("deleteByPart 메서드는")
    inner class DeleteByPartTest {
        @Test
        fun `정상적으로 파트가 일치하는 모든 스케줄을 삭제한다`() {
            // given
            val partId = 1L
            val part = PartFixtureBuilder().id(partId).build()
            whenever(partReader.readById(any())).thenReturn(part)
            // when and then
            assertThatCode { scheduleService.deleteByPart(1L) }.doesNotThrowAnyException()
        }

        @Test
        fun `없는 파트 정보로 시도할 경우 예외가 발생한다`() {
            // given
            val partId = 1L
            doThrow(PartNotFoundException::class.java).whenever(partReader).readById(partId)
            // when and then
            assertThatThrownBy { scheduleService.deleteByPart(partId) }
                .isInstanceOf(PartNotFoundException::class.java)
        }
    }

    @Nested
    @DisplayName("updateByPart 메서드는")
    inner class UpdateByPartTest {

        @Test
        fun `기존에 없던 스케줄은 생성한다`() {
            // given
            val partId = 1L
            val applicantId1 = 100L
            val applicantId2 = 101L
            val time1 = futureTime
            val time2 = futureTime.plus(1, ChronoUnit.HOURS)

            val part = PartFixtureBuilder().id(partId).build()
            val applicant1 = ApplicantFixtureBuilder().id(applicantId1).part(part).build()
            val applicant2 = ApplicantFixtureBuilder().id(applicantId2).part(part).build()

            // 기존: 10:00-지원자A
            val existingSchedules = listOf(
                ReadScheduleDto(
                    id = 1L,
                    applicantId = applicantId1,
                    applicantName = "지원자A",
                    part = "백엔드",
                    startTime = time1,
                    endTime = time1.plus(1, ChronoUnit.HOURS)
                )
            )

            // 요청: 10:00-지원자A, 11:00-지원자B
            val commands = listOf(
                CreateScheduleCommand(applicantId1, time1, time1.plus(1, ChronoUnit.HOURS), partId),
                CreateScheduleCommand(applicantId2, time2, time2.plus(1, ChronoUnit.HOURS), partId)
            )

            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(existingSchedules)
            whenever(partReader.readAllByIds(listOf(partId))).thenReturn(listOf(part))
            whenever(applicantReader.readByIdsWithoutAvailableTimes(listOf(applicantId1, applicantId2)))
                .thenReturn(listOf(applicant1, applicant2))
            doNothing().whenever(scheduleValidator).validateNoDuplicates(any())
            doNothing().whenever(scheduleWriter).deleteAll(any())
            doNothing().whenever(scheduleWriter).writeAll(any())

            // when
            scheduleService.updateByPart(partId, commands)

            // then
            val createCaptor = argumentCaptor<List<Schedule>>()
            // 삭제는 아예 호출이 되지 않아야 함
            verify(scheduleWriter, never()).deleteAll(anyList())
            verify(scheduleWriter).writeAll(createCaptor.capture())


            // 생성은 11:00-B만
            val created = createCaptor.firstValue
            assertThat(created).hasSize(1)
            assertThat(created[0].applicant.id).isEqualTo(applicantId2)
            assertThat(created[0].startTime).isEqualTo(time2)
        }

        @Test
        fun `사라진 스케줄은 삭제한다`() {
            // given
            val partId = 1L
            val applicantId1 = 100L
            val applicantId2 = 101L
            val time1 = futureTime
            val time2 = futureTime.plus(1, ChronoUnit.HOURS)

            val part = PartFixtureBuilder().id(partId).build()
            val applicant1 = ApplicantFixtureBuilder().id(applicantId1).part(part).build()

            // 기존: 10:00-지원자A, 11:00-지원자B
            val existingSchedules = listOf(
                ReadScheduleDto(
                    id = 1L,
                    applicantId = applicantId1,
                    applicantName = "지원자A",
                    part = "백엔드",
                    startTime = time1,
                    endTime = time1.plus(1, ChronoUnit.HOURS)
                ),
                ReadScheduleDto(
                    id = 2L,
                    applicantId = applicantId2,
                    applicantName = "지원자B",
                    part = "백엔드",
                    startTime = time2,
                    endTime = time2.plus(1, ChronoUnit.HOURS)
                )
            )

            // 요청: 10:00-지원자A만
            val commands = listOf(
                CreateScheduleCommand(applicantId1, time1, time1.plus(1, ChronoUnit.HOURS), partId)
            )

            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(existingSchedules)
            whenever(partReader.readAllByIds(listOf(partId))).thenReturn(listOf(part))
            whenever(applicantReader.readByIdsWithoutAvailableTimes(listOf(applicantId1)))
                .thenReturn(listOf(applicant1))
            doNothing().whenever(scheduleValidator).validateNoDuplicates(any())
            doNothing().whenever(scheduleWriter).deleteAll(any())
            doNothing().whenever(scheduleWriter).writeAll(any())

            // when
            scheduleService.updateByPart(partId, commands)

            // then
            val deleteCaptor = argumentCaptor<List<Long>>()
            verify(scheduleWriter).deleteAll(deleteCaptor.capture())
            // 생성은 아예 호출되지 않아야 함
            verify(scheduleWriter, never()).writeAll(anyList())

            // 11:00-B 삭제
            assertThat(deleteCaptor.firstValue).containsExactly(2L)
        }

        @Test
        fun `같은 시간대에 다른 면접자면 삭제 후 생성한다`() {
            // given
            val partId = 1L
            val applicantId1 = 100L
            val applicantId2 = 101L
            val time = futureTime

            val part = PartFixtureBuilder().id(partId).build()
            val applicant2 = ApplicantFixtureBuilder().id(applicantId2).part(part).build()

            // 기존: 10:00-지원자A
            val existingSchedules = listOf(
                ReadScheduleDto(
                    id = 1L,
                    applicantId = applicantId1,
                    applicantName = "지원자A",
                    part = "백엔드",
                    startTime = time,
                    endTime = time.plus(1, ChronoUnit.HOURS)
                )
            )

            // 요청: 10:00-지원자B (같은 시간, 다른 면접자)
            val commands = listOf(
                CreateScheduleCommand(applicantId2, time, time.plus(1, ChronoUnit.HOURS), partId)
            )

            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(existingSchedules)
            whenever(partReader.readAllByIds(listOf(partId))).thenReturn(listOf(part))
            whenever(applicantReader.readByIdsWithoutAvailableTimes(listOf(applicantId2)))
                .thenReturn(listOf(applicant2))
            doNothing().whenever(scheduleValidator).validateNoDuplicates(any())
            doNothing().whenever(scheduleWriter).deleteAll(any())
            doNothing().whenever(scheduleWriter).writeAll(any())

            // when
            scheduleService.updateByPart(partId, commands)

            // then
            val deleteCaptor = argumentCaptor<List<Long>>()
            val createCaptor = argumentCaptor<List<Schedule>>()
            verify(scheduleWriter).deleteAll(deleteCaptor.capture())
            verify(scheduleWriter).writeAll(createCaptor.capture())

            // 기존 10:00-A 삭제
            assertThat(deleteCaptor.firstValue).containsExactly(1L)

            // 새로운 10:00-B 생성
            val created = createCaptor.firstValue
            assertThat(created).hasSize(1)
            assertThat(created[0].applicant.id).isEqualTo(applicantId2)
            assertThat(created[0].startTime).isEqualTo(time)
        }

        @Test
        fun `같은 시간대 같은 면접자면 유지한다`() {
            // given
            val partId = 1L
            val applicantId = 100L
            val time = futureTime

            val part = PartFixtureBuilder().id(partId).build()
            val applicant = ApplicantFixtureBuilder().id(applicantId).part(part).build()

            // 기존: 10:00-지원자A
            val existingSchedules = listOf(
                ReadScheduleDto(
                    id = 1L,
                    applicantId = applicantId,
                    applicantName = "지원자A",
                    part = "백엔드",
                    startTime = time,
                    endTime = time.plus(1, ChronoUnit.HOURS)
                )
            )

            // 요청: 10:00-지원자A (같은 시간, 같은 면접자)
            val commands = listOf(
                CreateScheduleCommand(applicantId, time, time.plus(1, ChronoUnit.HOURS), partId)
            )

            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(existingSchedules)
            whenever(partReader.readAllByIds(listOf(partId))).thenReturn(listOf(part))
            whenever(applicantReader.readByIdsWithoutAvailableTimes(listOf(applicantId)))
                .thenReturn(listOf(applicant))
            doNothing().whenever(scheduleValidator).validateNoDuplicates(any())
            doNothing().whenever(scheduleWriter).deleteAll(any())
            doNothing().whenever(scheduleWriter).writeAll(any())

            // when
            scheduleService.updateByPart(partId, commands)

            // then
            // 삭제도 생성도 없어야 함 (유지)
            verify(scheduleWriter, never()).deleteAll(anyList())
            verify(scheduleWriter, never()).writeAll(anyList())
        }

        @Test
        fun `요청에 중복된 스케줄이 있으면 DuplicateScheduleException을 반환한다`() {
            // given
            val partId = 1L
            val applicantId1 = 100L
            val applicantId2 = 101L
            val sameTime = futureTime

            val part = PartFixtureBuilder().id(partId).build()
            val applicant1 = ApplicantFixtureBuilder().id(applicantId1).part(part).build()
            val applicant2 = ApplicantFixtureBuilder().id(applicantId2).part(part).build()

            // 요청: 같은 시간에 두 명의 면접자 (중복!)
            val commands = listOf(
                CreateScheduleCommand(applicantId1, sameTime, sameTime.plus(1, ChronoUnit.HOURS), partId),
                CreateScheduleCommand(applicantId2, sameTime, sameTime.plus(1, ChronoUnit.HOURS), partId)
            )

            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(emptyList())
            whenever(partReader.readAllByIds(listOf(partId))).thenReturn(listOf(part))
            whenever(applicantReader.readByIdsWithoutAvailableTimes(listOf(applicantId1, applicantId2)))
                .thenReturn(listOf(applicant1, applicant2))
            doThrow(DuplicateScheduleException::class.java).whenever(scheduleValidator).validateNoDuplicates(any())

            // when and then
            assertThatThrownBy {
                scheduleService.updateByPart(partId, commands)
            }.isInstanceOf(DuplicateScheduleException::class.java)
        }
    }
}