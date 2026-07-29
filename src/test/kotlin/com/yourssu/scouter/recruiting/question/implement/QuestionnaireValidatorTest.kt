package com.yourssu.scouter.recruiting.question.implement

import com.yourssu.scouter.recruiting.support.implement.exception.FixedQuestionNotFoundException
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionnaireGroupInvalidException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class QuestionnaireValidatorTest {

    private val validator = QuestionnaireValidator()

    private val applicantId = 1L

    private fun cultureFitFixed(sourceQuestionId: Long) = QuestionnaireQuestion(
        questionnaireId = applicantId,
        group = QuestionGroup.CULTURE_FIT,
        sourceQuestionId = sourceQuestionId,
        content = null,
        sortOrder = 0,
    )

    private fun freeQuestion(group: QuestionGroup, content: String) = QuestionnaireQuestion(
        questionnaireId = applicantId,
        group = group,
        sourceQuestionId = null,
        content = content,
        sortOrder = 0,
    )

    @Test
    fun `CULTURE_FIT 문항이 2개 미만이면 예외를 발생시킨다`() {
        val fixedQuestionsById = mapOf<Long?, FixedQuestion>(
            1L to FixedQuestion(1L, FixedQuestionCategory.CULTURE_FIT, "질문", 1),
        )

        assertThatThrownBy { validator.validate(listOf(cultureFitFixed(1L)), fixedQuestionsById) }
            .isInstanceOf(QuestionnaireGroupInvalidException::class.java)
    }

    @Test
    fun `존재하지 않는 고정 질문을 참조하면 예외를 발생시킨다`() {
        val questions = listOf(cultureFitFixed(1L), cultureFitFixed(2L))

        assertThatThrownBy { validator.validate(questions, emptyMap()) }
            .isInstanceOf(FixedQuestionNotFoundException::class.java)
    }

    @Test
    fun `고정 질문의 카테고리가 그룹과 다르면 예외를 발생시킨다`() {
        val fixedQuestionsById = mapOf<Long?, FixedQuestion>(
            1L to FixedQuestion(1L, FixedQuestionCategory.REQUIRED, "질문1", 1),
            2L to FixedQuestion(2L, FixedQuestionCategory.CULTURE_FIT, "질문2", 1),
        )
        val questions = listOf(cultureFitFixed(1L), cultureFitFixed(2L))

        assertThatThrownBy { validator.validate(questions, fixedQuestionsById) }
            .isInstanceOf(QuestionnaireGroupInvalidException::class.java)
    }

    @Test
    fun `모든 조건을 만족하면 예외 없이 통과한다`() {
        val fixedQuestionsById = mapOf<Long?, FixedQuestion>(
            1L to FixedQuestion(1L, FixedQuestionCategory.CULTURE_FIT, "질문1", 1),
            2L to FixedQuestion(2L, FixedQuestionCategory.CULTURE_FIT, "질문2", 2),
        )
        val questions = listOf(cultureFitFixed(1L), cultureFitFixed(2L), freeQuestion(QuestionGroup.PERSONAL, "자유 문항"))

        assertThatCode { validator.validate(questions, fixedQuestionsById) }.doesNotThrowAnyException()
    }
}
