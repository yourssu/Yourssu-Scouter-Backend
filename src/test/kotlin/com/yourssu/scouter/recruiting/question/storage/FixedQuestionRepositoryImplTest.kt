package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.recruiting.question.implement.FixedQuestionCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(FixedQuestionRepositoryImpl::class)
@Suppress("NonAsciiCharacters")
class FixedQuestionRepositoryImplTest {

    @Autowired
    lateinit var fixedQuestionRepositoryImpl: FixedQuestionRepositoryImpl

    @Autowired
    lateinit var entityManager: TestEntityManager

    private var requiredQuestionId: Long = 0
    private var cultureFitQuestionId: Long = 0

    @BeforeEach
    fun setUp() {
        val required = entityManager.persist(
            FixedQuestionEntity(category = FixedQuestionCategory.REQUIRED, content = "자기소개를 해주세요.", sortOrder = 1),
        )
        val cultureFit = entityManager.persist(
            FixedQuestionEntity(category = FixedQuestionCategory.CULTURE_FIT, content = "협업 경험을 말씀해주세요.", sortOrder = 1),
        )
        requiredQuestionId = required.id!!
        cultureFitQuestionId = cultureFit.id!!

        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `저장된 고정 질문 전체를 조회한다`() {
        val result = fixedQuestionRepositoryImpl.findAll()

        assertThat(result).hasSize(2)
        assertThat(result.map { it.content }).containsExactlyInAnyOrder("자기소개를 해주세요.", "협업 경험을 말씀해주세요.")
    }

    @Test
    fun `id 목록으로 고정 질문을 조회한다`() {
        val result = fixedQuestionRepositoryImpl.findAllByIdIn(listOf(requiredQuestionId))

        assertThat(result).hasSize(1)
        assertThat(result[0].category).isEqualTo(FixedQuestionCategory.REQUIRED)
    }

    @Test
    fun `존재하지 않는 id 목록으로 조회하면 빈 리스트를 반환한다`() {
        val result = fixedQuestionRepositoryImpl.findAllByIdIn(listOf(99999L))

        assertThat(result).isEmpty()
    }
}
