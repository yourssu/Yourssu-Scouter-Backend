package com.yourssu.scouter.ats.implement.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import com.yourssu.scouter.common.implement.domain.division.Division
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import com.yourssu.scouter.common.implement.domain.semester.Term
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import java.sql.SQLException
import java.time.Instant
import java.time.LocalDateTime
import java.time.Year

private const val EXCEPTION_MESSAGE = "이미 해당 시간에 면접이 예정되어 있습니다."

@ExtendWith(MockitoExtension::class)
@Suppress("NonAsciiCharacters")
class ScheduleWriterTest {

    @Mock
    private lateinit var scheduleRepository: ScheduleRepository

    @InjectMocks
    private lateinit var scheduleWriter: ScheduleWriter

    private lateinit var testSchedules: List<Schedule>

    @BeforeEach
    fun setup() {
        val division = Division(id = 1L, name = "개발", sortPriority = 1)
        val part = Part(1, name = "백엔드", sortPriority = 1, division = division)
        testSchedules = listOf(
            Schedule(
                id = 1L,
                part = part,
                applicant = createTestApplicant(part),
                startTime = Instant.parse("2025-09-15T10:00:00Z"),
                endTime = Instant.parse("2025-09-15T11:00:00Z")
            )
        )
    }

    @Test
    fun `writeAll은 정상적으로 스케줄을 저장한다`() {
        // given

        // when
        scheduleWriter.writeAll(testSchedules)

        // then
        verify(scheduleRepository).saveAll(testSchedules)
    }

    @Test
    fun `writeAll은 DuplicateKeyException 발생시 DuplicateScheduleException으로 변환한다`() {
        // given
        whenever(scheduleRepository.saveAll(any()))
            .doThrow(DuplicateKeyException("Duplicate key"))

        // when and then
        assertThatThrownBy { scheduleWriter.writeAll(testSchedules) }
            .isInstanceOf(DuplicateScheduleException::class.java)
            .hasMessage(EXCEPTION_MESSAGE)
    }

    @Test
    fun `writeAll은 H2의 SQLException(23505) 발생시 DuplicateScheduleException으로 변환한다`() {
        // given
        val sqlException = SQLException("Unique index violation", "23505")
        val dataIntegrityException = DataIntegrityViolationException("constraint violation", sqlException)

        whenever(scheduleRepository.saveAll(any()))
            .doThrow(dataIntegrityException)

        // when and then
        assertThatThrownBy { scheduleWriter.writeAll(testSchedules) }
            .isInstanceOf(DuplicateScheduleException::class.java)
            .hasMessage(EXCEPTION_MESSAGE)
    }

    @Test
    fun `writeAll은 Hibernate ConstraintViolationException 발생시 DuplicateScheduleException으로 변환한다`() {
        // given
        val sqlException = SQLException("constraint violation")
        val constraintViolation = ConstraintViolationException("constraint", sqlException, "unique_constraint")
        val dataIntegrityException = DataIntegrityViolationException("constraint violation", constraintViolation)

        whenever(scheduleRepository.saveAll(any()))
            .doThrow(dataIntegrityException)

        // when and then
        assertThatThrownBy { scheduleWriter.writeAll(testSchedules) }
            .isInstanceOf(DuplicateScheduleException::class.java)
            .hasMessage(EXCEPTION_MESSAGE)
    }

    @Test
    fun `writeAll은 다른 DataIntegrityViolationException은 그대로 던진다`() {
        // given
        val otherException = DataIntegrityViolationException("Other integrity violation")

        whenever(scheduleRepository.saveAll(any()))
            .doThrow(otherException)

        // when and then
        assertThatThrownBy { scheduleWriter.writeAll(testSchedules) }
            .isInstanceOf(DataIntegrityViolationException::class.java)
            .hasMessageNotContaining(EXCEPTION_MESSAGE)
    }


    private fun createTestApplicant(part: Part) = Applicant(
        id = 1L,
        name = "김철수",
        email = "test@example.com",
        phoneNumber = "010-1234-5678",
        age = "22",
        department = "컴퓨터공학부",
        studentId = "20210001",
        part = part,
        state = ApplicantState.UNDER_REVIEW,
        applicationDateTime = LocalDateTime.of(2025, 9, 15, 10, 0),
        applicationSemester = Semester(1L, Year.of(2025), Term.SPRING),
        academicSemester = "2-2",
        availableTimes = emptyList(),
    )
}