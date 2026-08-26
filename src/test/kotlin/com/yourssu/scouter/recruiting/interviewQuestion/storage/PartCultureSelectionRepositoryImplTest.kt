package com.yourssu.scouter.recruiting.interviewQuestion.storage

import com.yourssu.scouter.masterdata.division.storage.DivisionEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.masterdata.semester.implement.Term
import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import
import java.time.Year

@DataJpaTest
@Import(PartCultureSelectionRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class PartCultureSelectionRepositoryImplTest {

    @Autowired
    lateinit var partCultureSelectionRepositoryImpl: PartCultureSelectionRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    private var partId: Long = 0
    private var semesterId: Long = 0
    private var questionId1: Long = 0
    private var questionId2: Long = 0
    private var questionId3: Long = 0

    @BeforeEach
    fun setUp() {
        val division = entityManager.persist(DivisionEntity(null, "개발", 1))
        val part = entityManager.persist(PartEntity(null, division, "PM", 1))
        val semester = entityManager.persist(SemesterEntity(null, Year.of(2025), Term.FALL))

        partId = part.id!!
        semesterId = semester.id!!

        questionId1 = entityManager.persist(
            QuestionEntity(null, null, semesterId, QuestionCategory.CULTURE, "컬쳐1", 0),
        ).id!!
        questionId2 = entityManager.persist(
            QuestionEntity(null, null, semesterId, QuestionCategory.CULTURE, "컬쳐2", 1),
        ).id!!
        questionId3 = entityManager.persist(
            QuestionEntity(null, null, semesterId, QuestionCategory.CULTURE, "컬쳐3", 2),
        ).id!!

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `선택 집합이 겹치지 않으면 이전 선택을 지우고 새 선택으로 치환한다`() {
        partCultureSelectionRepositoryImpl.replaceSelection(partId, semesterId, listOf(questionId1, questionId2))

        partCultureSelectionRepositoryImpl.replaceSelection(partId, semesterId, listOf(questionId3))

        val found = partCultureSelectionRepositoryImpl.findSelectedQuestionIds(partId, semesterId)
        assertThat(found).containsExactly(questionId3)
    }

    @Test
    fun `선택 집합이 겹칠 때(가장 흔한 경로) 겹치는 항목은 유지하고 차이만 반영한다`() {
        partCultureSelectionRepositoryImpl.replaceSelection(partId, semesterId, listOf(questionId1, questionId2))

        // questionId1은 그대로 유지, questionId2는 제거, questionId3은 새로 추가되는 겹치는 케이스.
        partCultureSelectionRepositoryImpl.replaceSelection(partId, semesterId, listOf(questionId1, questionId3))

        val found = partCultureSelectionRepositoryImpl.findSelectedQuestionIds(partId, semesterId)
        assertThat(found).containsExactlyInAnyOrder(questionId1, questionId3)
    }

    @Test
    fun `동일한 선택으로 다시 치환해도 결과가 유지된다`() {
        partCultureSelectionRepositoryImpl.replaceSelection(partId, semesterId, listOf(questionId1, questionId2))

        partCultureSelectionRepositoryImpl.replaceSelection(partId, semesterId, listOf(questionId1, questionId2))

        val found = partCultureSelectionRepositoryImpl.findSelectedQuestionIds(partId, semesterId)
        assertThat(found).containsExactlyInAnyOrder(questionId1, questionId2)
    }
}
