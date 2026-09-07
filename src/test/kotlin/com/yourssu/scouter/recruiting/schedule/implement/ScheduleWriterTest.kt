package com.yourssu.scouter.recruiting.schedule.implement

import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.masterdata.division.implement.Division
import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.implement.Term
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import java.time.Instant
import java.time.Year

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
        val part = Part(1, name = "백엔드", sortPriority = 1, division = division, hasAssignment = false)
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
        // when
        scheduleWriter.writeAll(testSchedules)

        // then
        verify(scheduleRepository).saveAll(testSchedules)
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
        applicationDateTime = Instant.parse("2025-09-15T10:00:00Z"),
        applicationSemester = Semester(1L, Year.of(2025), Term.SPRING),
        academicSemester = "2-2",
        availableTimes = emptyList(),
    )
}
