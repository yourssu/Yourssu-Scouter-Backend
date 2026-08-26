package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.implement.Term
import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirement
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.Year

@DataJpaTest
@Import(InterviewRequirementRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class InterviewRequirementRepositoryImplTest {

    @Autowired
    lateinit var partInterviewRequirementRepositoryImpl: InterviewRequirementRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    private var partId: Long = 0

    @BeforeEach
    fun setUp() {
        val division = entityManager.persist(DivisionEntity(null, "개발", 1))
        val part = entityManager.persist(PartEntity(null, division, "PM", 1))
        partId = part.id!!

        entityManager.persist(SemesterEntity(null, Year.of(2026), Term.SPRING))

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `설정된 요구조건이 없으면 빈 리스트를 반환한다`() {
        // when
        val result = partInterviewRequirementRepositoryImpl.findAllByPartIdAndSemester(partId, Semester.of("2026-1"))

        // then
        assertThat(result).isEmpty()
    }

    @Test
    fun `요구조건을 저장하고 조회한다`() {
        // given
        val semester = Semester.of("2026-1")
        val requirements = listOf(
            InterviewRequirement(null, partId, 0L, RubricGroupType.CULTURE, "주도성"),
            InterviewRequirement(null, partId, 0L, RubricGroupType.TEAM, "커뮤니케이션"),
            InterviewRequirement(null, partId, 0L, RubricGroupType.JOB, "문제 구조화")
        )

        // when
        val saved = partInterviewRequirementRepositoryImpl.saveAll(requirements, partId, semester)
        entityManager.flush()
        entityManager.clear()

        // then
        val found = partInterviewRequirementRepositoryImpl.findAllByPartIdAndSemester(partId, semester)
        assertThat(found).hasSize(3)
        assertThat(found).extracting<String> { it.content }
            .containsExactlyInAnyOrder("주도성", "커뮤니케이션", "문제 구조화")
    }
}
