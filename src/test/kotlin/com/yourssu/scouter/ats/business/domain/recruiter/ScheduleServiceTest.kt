package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantReader
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.ats.implement.domain.recruiter.ReadScheduleDto
import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleReader
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleWriter
import com.yourssu.scouter.ats.implement.support.exception.ApplicantNotFoundException
import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import com.yourssu.scouter.common.implement.domain.division.Division
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.Term
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
import java.time.Year

@Suppress("NonAsciiCharacters")
class ScheduleServiceTest {

    private lateinit var scheduleService: ScheduleService
    private lateinit var scheduleWriter: ScheduleWriter
    private lateinit var scheduleReader: ScheduleReader
    private lateinit var partReader: PartReader
    private lateinit var applicantReader: ApplicantReader
    private lateinit var scheduleValidator: ScheduleValidator

    private val futureTime = LocalDateTime.now().plusDays(7)
    private val testDivision = Division(id = 1L, name = "개발", sortPriority = 1)
    private val testSemester = Semester(id = 1L, year = Year.of(2025), term = Term.SPRING)

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

            val part = Part(
                id = partId,
                division = testDivision,
                name = "Backend",
                sortPriority = 1
            )
            val applicant = Applicant(
                id = applicantId,
                name = "홍길동",
                email = "test@example.com",
                phoneNumber = "010-1234-5678",
                age = "22",
                department = "컴퓨터공학과",
                studentId = "2021001",
                part = part,
                state = ApplicantState.UNDER_REVIEW,
                applicationDateTime = LocalDateTime.now().minusDays(1),
                applicationSemester = testSemester,
                academicSemester = "7학기",
                availableTimes = emptyList()
            )

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

            val part = Part(
                id = partId,
                division = testDivision,
                name = "Backend",
                sortPriority = 1
            )
            val applicant1 = createTestApplicant(applicantId1, "홍길동", part)
            val applicant2 = createTestApplicant(applicantId2, "김철수", part)

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
                    createTestApplicant(
                        applicantId,
                        "홍길동",
                        Part(1L, testDivision, "Backend", 1)
                    )
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

            val part = Part(
                id = partId,
                division = testDivision,
                name = "Backend",
                sortPriority = 1
            )
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
                    part = "Backend"
                ),
                ReadScheduleDto(
                    id = 2L,
                    name = "김철수",
                    interviewTime = futureTime.plusHours(1),
                    part = "Backend"
                )
            )

            whenever(scheduleReader.readAllByPartId(partId)).thenReturn(schedules)

            // when
            val result = scheduleService.readSchedulesByPartId(partId)

            // then
            assertThat(result).hasSize(2)
            assertThat(result[0].name).isEqualTo("홍길동")
            assertThat(result[1].name).isEqualTo("김철수")
            assertThat(result.map { it.part }).allMatch { it == "Backend" }
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

    private fun createTestApplicant(id: Long, name: String, part: Part): Applicant {
        return Applicant(
            id = id,
            name = name,
            email = "test@example.com",
            phoneNumber = "010-1234-5678",
            age = "22",
            department = "컴퓨터공학과",
            studentId = "2021001",
            part = part,
            state = ApplicantState.UNDER_REVIEW,
            applicationDateTime = LocalDateTime.now().minusDays(1),
            applicationSemester = testSemester,
            academicSemester = "7학기",
            availableTimes = emptyList()
        )
    }
}