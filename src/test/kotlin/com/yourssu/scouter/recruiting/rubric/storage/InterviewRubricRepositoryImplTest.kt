package com.yourssu.scouter.recruiting.rubric.storage

import com.yourssu.scouter.common.division.storage.DivisionEntity
import com.yourssu.scouter.common.part.storage.PartEntity
import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.common.semester.implement.Term
import com.yourssu.scouter.common.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationItem
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricMapper
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.Instant
import java.time.Year

@DataJpaTest
@Import(InterviewRubricRepositoryImpl::class, InterviewRubricMapper::class)
class InterviewRubricRepositoryImplTest {

    @Autowired
    lateinit var interviewRubricRepository: InterviewRubricRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    private var partId: Long = 0

    @BeforeEach
    fun setUp() {
        val division = entityManager.persist(DivisionEntity(null, "개발", 1))
        val part = entityManager.persist(PartEntity(null, division, "PM", 1))
        partId = part.id!!
        
        // Persist SemesterEntities to satisfy FK constraint
        entityManager.persist(SemesterEntity(null, Year.of(2026), Term.SPRING))
        entityManager.persist(SemesterEntity(null, Year.of(2026), Term.FALL))
        
        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `루브릭과 평가 항목을 저장한 뒤 파트 ID로 조회한다`() {
        val saved = interviewRubricRepository.save(rubric())

        entityManager.flush()
        entityManager.clear()

        val found = interviewRubricRepository.getByPartIdAndSemester(partId, Semester.of("2026-2"))

        assertThat(found.id).isEqualTo(saved.id)
        assertThat(found.partId).isEqualTo(partId)
        assertThat(found.items).extracting<String> { it.keyword }
            .containsExactly("직무 역량", "협업 역량")
    }

    @Test
    fun `같은 파트라도 학기별 루브릭을 각각 조회한다`() {
        interviewRubricRepository.save(rubric(Semester.of("2026-1")))
        interviewRubricRepository.save(rubric(Semester.of("2026-2")))

        entityManager.flush()
        entityManager.clear()

        assertThat(interviewRubricRepository.getByPartIdAndSemester(partId, Semester.of("2026-1")).semester)
            .isEqualByComparingTo(Semester.of("2026-1"))
        assertThat(interviewRubricRepository.getByPartIdAndSemester(partId, Semester.of("2026-2")).semester)
            .isEqualByComparingTo(Semester.of("2026-2"))
    }

    private fun rubric(semester: Semester = Semester.of("2026-2")) = InterviewRubric(
        partId = partId,
        semester = semester,
        deadline = Instant.parse("2026-08-01T00:00:00Z"),
        items = listOf(
            InterviewEvaluationItem(keyword = "직무 역량", rubricType = RubricGroupType.JOB, maxScore = 60),
            InterviewEvaluationItem(keyword = "협업 역량", rubricType = RubricGroupType.TEAM, maxScore = 40),
        ),
    )
}
