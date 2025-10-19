package com.yourssu.scouter.ats.storage.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.ats.storage.domain.applicant.ApplicantEntity
import com.yourssu.scouter.common.implement.domain.semester.Term
import com.yourssu.scouter.common.storage.domain.division.DivisionEntity
import com.yourssu.scouter.common.storage.domain.part.PartEntity
import com.yourssu.scouter.common.storage.domain.semester.SemesterEntity
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.dao.DataIntegrityViolationException
import java.time.LocalDateTime
import java.time.Year

@DataJpaTest
@Suppress("NonAsciiCharacters")
class JpaScheduleRepositoryTest {

    @Autowired
    lateinit var jpaScheduleRepository: JpaScheduleRepository

    @Autowired
    lateinit var entityManager: TestEntityManager

    private lateinit var savedPart: PartEntity
    private lateinit var savedApplicant: ApplicantEntity

    companion object {
        private val STANDARD_INTERVIEW_TIME =
            LocalDateTime.of(2025, 12, 15, 14, 0)

        private val ALTERNATIVE_INTERVIEW_TIME =
            LocalDateTime.of(2025, 12, 15, 15, 0)
    }

    @BeforeEach
    fun setUp() {
        savedPart = createMinimalPart()
        savedApplicant = createMinimalApplicant(savedPart)

        flushAndClear()
    }

    @Test
    fun `스케줄을 저장하고 조회할 수 있다`() {
        // given
        val schedule = createSchedule(part = savedPart, applicant = savedApplicant)

        // when
        val saved = jpaScheduleRepository.save(schedule)
        flushAndClear()

        val found = jpaScheduleRepository.findById(saved.id!!)

        // then
        assertThat(found).isPresent
            .hasValueSatisfying {
                assertThat(it.interviewTime).isEqualTo(schedule.interviewTime)
                assertThat(it.part.id).isEqualTo(savedPart.id)
                assertThat(it.applicant.id).isEqualTo(savedApplicant.id)
            }
    }

    @Test
    fun `같은 파트와 면접 시간으로 중복 스케줄을 저장하면 예외가 발생한다`() {
        // given
        val schedule = createSchedule(part = savedPart, applicant = savedApplicant)
        val duplicatedSchedule = createSchedule(part = savedPart, applicant = savedApplicant)
        jpaScheduleRepository.save(schedule)
        flushAndClear()

        // when and then
        assertThatThrownBy {
            jpaScheduleRepository.save(duplicatedSchedule)
            entityManager.flush()
        }.isInstanceOf(DataIntegrityViolationException::class.java)

        entityManager.clear()
    }

    @Test
    fun `findAllWithNamesByPartId는 파트ID로 스케줄과 연관된 이름들을 포함한 DTO를 반환한다`() {
        // given
        val schedule = createSchedule(part = savedPart, applicant = savedApplicant)
        jpaScheduleRepository.save(schedule)
        flushAndClear()

        // when
        val schedules = jpaScheduleRepository.findAllWithNamesByPartId(savedPart.id!!)

        // then
        assertThat(schedules).hasSize(1)
            .element(0)
            .extracting("interviewTime", "partName", "applicantName")
            .containsExactly(STANDARD_INTERVIEW_TIME, savedPart.name, savedApplicant.name)

    }

    @Test
    fun `다른 면접 시간이면 같은 파트에 여러 스케줄을 저장할 수 있다`() {
        // given
        val schedule1 = createSchedule(part = savedPart, applicant = savedApplicant)
        val schedule2 = createSchedule(
            interviewTime = ALTERNATIVE_INTERVIEW_TIME,
            part = savedPart, applicant = savedApplicant
        )

        // when
        jpaScheduleRepository.save(schedule1)
        jpaScheduleRepository.save(schedule2)
        flushAndClear()

        // then
        val schedules = jpaScheduleRepository.findAllByPartId(savedPart.id!!)
        assertThat(schedules).hasSize(2)
            .extracting("interviewTime")
            .containsExactlyInAnyOrder(STANDARD_INTERVIEW_TIME, ALTERNATIVE_INTERVIEW_TIME)
    }

    @Test
    fun `findAllByPartId는 해당 파트의 모든 스케줄을 조회한다`() {
        // given

        val schedule1 = createSchedule(STANDARD_INTERVIEW_TIME, savedPart, savedApplicant)
        val schedule2 = createSchedule(ALTERNATIVE_INTERVIEW_TIME, savedPart, savedApplicant)

        jpaScheduleRepository.save(schedule1)
        jpaScheduleRepository.save(schedule2)
        flushAndClear()

        // when
        val schedules = jpaScheduleRepository.findAllByPartId(savedPart.id!!)

        // then
        assertThat(schedules).hasSize(2)
            .extracting("interviewTime")
            .containsExactlyInAnyOrder(STANDARD_INTERVIEW_TIME, ALTERNATIVE_INTERVIEW_TIME)
    }

    @Test
    fun `과거 면접 시간의 스케줄도 정상적으로 저장하고 조회할 수 있다`() {
        // given: 과거 시간
        val pastTime = LocalDateTime.of(2024, 1, 1, 10, 0)
        val schedule = createSchedule(pastTime, savedPart, savedApplicant)

        // when
        jpaScheduleRepository.save(schedule)
        flushAndClear()

        val schedules = jpaScheduleRepository.findAllByPartId(savedPart.id!!)

        // then
        assertThat(schedules).hasSize(1).element(0).extracting("interviewTime").isEqualTo(pastTime)
    }

    // ===== Helper Methods =====

    private fun flushAndClear() {
        entityManager.flush()
        entityManager.clear()
    }

    fun createSchedule(
        interviewTime: LocalDateTime = STANDARD_INTERVIEW_TIME, part: PartEntity, applicant: ApplicantEntity
    ) = ScheduleEntity(null, part, applicant, interviewTime)

    fun createMinimalPart(): PartEntity {
        val division = entityManager.persist(DivisionEntity(null, "개발", 1))
        return entityManager.persist(PartEntity(null, division, "백엔드", 1))
    }

    fun createMinimalApplicant(part: PartEntity = createMinimalPart()): ApplicantEntity {
        val semester = entityManager.persist(SemesterEntity(null, Year.of(2025), Term.SPRING))
        return entityManager.persist(ApplicantEntity(
            id = null,
            name = "김철수",
            email = "test@example.com",
            phoneNumber = "010-1234-5678",
            age = "22",
            department = "컴퓨터공학부",
            studentId = "20210001",
            part = part,
            state = ApplicantState.UNDER_REVIEW,
            applicationDateTime = LocalDateTime.of(2025, 9, 15, 10, 0),
            applicationSemester = semester,
            academicSemester = "2-2"
        ))
    }
}
