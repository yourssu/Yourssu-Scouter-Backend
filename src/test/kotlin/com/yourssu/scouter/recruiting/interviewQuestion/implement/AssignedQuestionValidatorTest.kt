package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionInvalidException
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionNotFoundException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class AssignedQuestionValidatorTest {

    private val validator = AssignedQuestionValidator()

    @Test
    fun `선택된 CULTURE 문항이 2개 미만이면 예외를 발생시킨다`() {
        val questions = listOf(
            catalogQuestion(1L, AssignedQuestionCategory.CULTURE, isSelected = true),
            catalogQuestion(2L, AssignedQuestionCategory.CULTURE, isSelected = false),
            catalogQuestion(3L, AssignedQuestionCategory.CULTURE, isSelected = false),
            catalogQuestion(4L, AssignedQuestionCategory.CULTURE, isSelected = false),
        )
        val sourceQuestionsById = mapOf<Long?, Question>(
            1L to Question(1L, null, null, QuestionCategory.CULTURE, "질문1", 1, requirementIds = listOf(1L)),
            2L to Question(2L, null, null, QuestionCategory.CULTURE, "질문2", 2, requirementIds = listOf(1L)),
            3L to Question(3L, null, null, QuestionCategory.CULTURE, "질문3", 3, requirementIds = listOf(1L)),
            4L to Question(4L, null, null, QuestionCategory.CULTURE, "질문4", 4, requirementIds = listOf(1L)),
        )

        assertThatThrownBy { validator.validate(questions, sourceQuestionsById, applicantPartId = 1L) }
            .isInstanceOf(AssignedQuestionInvalidException::class.java)
    }

    @Test
    fun `존재하지 않는 카탈로그 질문이면 예외를 발생시킨다`() {
        val questions = listOf(
            catalogQuestion(1L, AssignedQuestionCategory.CULTURE, isSelected = true),
            catalogQuestion(2L, AssignedQuestionCategory.CULTURE, isSelected = true),
        )

        assertThatThrownBy { validator.validate(questions, emptyMap(), applicantPartId = 1L) }
            .isInstanceOf(QuestionNotFoundException::class.java)
    }

    @Test
    fun `카탈로그 질문 카테고리와 할당 질문 카테고리가 다르면 예외를 발생시킨다`() {
        val questions = listOf(
            catalogQuestion(1L, AssignedQuestionCategory.CULTURE, isSelected = true),
            catalogQuestion(2L, AssignedQuestionCategory.CULTURE, isSelected = true),
        )
        val sourceQuestionsById = mapOf<Long?, Question>(
            1L to Question(1L, null, null, QuestionCategory.INTRO, "질문1", 1),
            2L to Question(2L, null, null, QuestionCategory.CULTURE, "질문2", 2, requirementIds = listOf(1L)),
        )

        assertThatThrownBy { validator.validate(questions, sourceQuestionsById, applicantPartId = 1L) }
            .isInstanceOf(AssignedQuestionInvalidException::class.java)
    }

    @Test
    fun `파트 질문이 지원자의 파트와 다르면 예외를 발생시킨다`() {
        val questions = listOf(
            catalogQuestion(1L, AssignedQuestionCategory.PART),
            catalogQuestion(2L, AssignedQuestionCategory.CULTURE, isSelected = true),
            catalogQuestion(3L, AssignedQuestionCategory.CULTURE, isSelected = true),
        )
        val sourceQuestionsById = mapOf<Long?, Question>(
            1L to Question(1L, 2L, null, QuestionCategory.PART, "파트 질문", 1, requirementIds = listOf(1L)),
            2L to Question(2L, null, null, QuestionCategory.CULTURE, "컬쳐 질문1", 1, requirementIds = listOf(1L)),
            3L to Question(3L, null, null, QuestionCategory.CULTURE, "컬쳐 질문2", 2, requirementIds = listOf(1L)),
        )

        assertThatThrownBy { validator.validate(questions, sourceQuestionsById, applicantPartId = 1L) }
            .isInstanceOf(AssignedQuestionInvalidException::class.java)
    }

    private fun catalogQuestion(
        sourceQuestionId: Long,
        category: AssignedQuestionCategory,
        isSelected: Boolean? = null,
    ): AssignedQuestion {
        return AssignedQuestion(
            assignedMemberId = 1L,
            applicantId = 1L,
            sourceQuestionId = sourceQuestionId,
            content = null,
            category = category,
            sortOrder = 0,
            isSelected = isSelected,
        )
    }
}
