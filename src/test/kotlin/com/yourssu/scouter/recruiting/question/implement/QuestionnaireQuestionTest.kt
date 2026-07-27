package com.yourssu.scouter.recruiting.question.implement

import com.yourssu.scouter.recruiting.support.implement.exception.QuestionnaireGroupInvalidException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class QuestionnaireQuestionTest {

    private val applicantId = 1L

    @Test
    fun `고정 그룹 문항에 sourceQuestionId가 없으면 예외를 발생시킨다`() {
        assertThatThrownBy {
            QuestionnaireQuestion(
                questionnaireId = applicantId,
                group = QuestionGroup.CULTURE_FIT,
                sourceQuestionId = null,
                content = null,
                sortOrder = 0,
            )
        }.isInstanceOf(QuestionnaireGroupInvalidException::class.java)
    }

    @Test
    fun `자유 그룹 문항에 content가 없으면 예외를 발생시킨다`() {
        assertThatThrownBy {
            QuestionnaireQuestion(
                questionnaireId = applicantId,
                group = QuestionGroup.PERSONAL,
                sourceQuestionId = null,
                content = " ",
                sortOrder = 0,
            )
        }.isInstanceOf(QuestionnaireGroupInvalidException::class.java)
    }

    @Test
    fun `고정 그룹 문항에 sourceQuestionId가 있으면 정상 생성된다`() {
        assertThatCode {
            QuestionnaireQuestion(
                questionnaireId = applicantId,
                group = QuestionGroup.CULTURE_FIT,
                sourceQuestionId = 1L,
                content = null,
                sortOrder = 0,
            )
        }.doesNotThrowAnyException()
    }

    @Test
    fun `자유 그룹 문항에 content가 있으면 정상 생성된다`() {
        assertThatCode {
            QuestionnaireQuestion(
                questionnaireId = applicantId,
                group = QuestionGroup.PERSONAL,
                sourceQuestionId = null,
                content = "자유 문항",
                sortOrder = 0,
            )
        }.doesNotThrowAnyException()
    }
}
