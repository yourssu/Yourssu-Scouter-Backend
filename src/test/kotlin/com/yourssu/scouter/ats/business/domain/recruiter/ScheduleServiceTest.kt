package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.recruiter.ReadScheduleDto
import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleReader
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleWriter
import com.yourssu.scouter.ats.implement.support.exception.ApplicantNotFoundException
import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import com.yourssu.scouter.common.fixture.PartFixtureBuilder
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.support.exception.PartNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@Suppress("NonAsciiCharacters")
class ScheduleServiceTest {

    private lateinit var scheduleService: ScheduleService
    private lateinit var scheduleWriter: ScheduleWriter
    private lateinit var scheduleReader: ScheduleReader
    private lateinit var partReader: PartReader
    private lateinit var applicantReader: ApplicantReader
    private lateinit var scheduleValidator: ScheduleValidator

    private val futureTime = LocalDateTime.now().plusDays(7)

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
            scheduleValidator
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
                interviewTime = futureTime,
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
            assertThat(savedSchedules[0].interviewTime).isEqualTo(futureTime)
        }

        @Test
        fun `중복된 스케줄 생성 요청시 예외가 발생한다`() {
            // given
            val partId = 1L
            val applicantId1 = 100L
            val applicantId2 = 101L
            val sameTime = futureTime

            val commands = listOf(
                CreateScheduleCommand(applicantId1, sameTime, partId),
                CreateScheduleCommand(applicantId2, sameTime, partId)
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
            val command = CreateScheduleCommand(applicantId, futureTime, invalidPartId)

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
            val command = CreateScheduleCommand(invalidApplicantId, futureTime, partId)

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
    @DisplayName("readSchedulesByPartId 메서드는")
    inner class ReadSchedulesByPartIdTests {

        @Test
        fun `파트 ID로 스케줄 목록을 조회한다`() {
            // given
            val partId = 1L
            val schedules = listOf(
                ReadScheduleDto(
                    id = 1L,
                    name = "홍길동",
                    interviewTime = futureTime,
                    part = "백엔드"
                ),
                ReadScheduleDto(
                    id = 2L,
                    name = "김철수",
                    interviewTime = futureTime.plusHours(1),
                    part = "백엔드"
                )
            )

            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(schedules)

            // when
            val result = scheduleService.readSchedulesByPartId(partId)

            // then
            assertThat(result).hasSize(2)
            assertThat(result[0].name).isEqualTo("홍길동")
            assertThat(result[1].name).isEqualTo("김철수")
            assertThat(result.map { it.part }).allMatch { it == "백엔드" }
        }

        @Test
        fun `해당 파트에 스케줄이 없으면 빈 리스트를 반환한다`() {
            // given
            val partId = 1L
            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(emptyList())

            // when
            val result = scheduleService.readSchedulesByPartId(partId)

            // then
            assertThat(result).isEmpty()
        }
    }

}