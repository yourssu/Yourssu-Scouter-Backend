package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class QuestionTest {

    @Test
    fun `PART 질문은 요구조건이 최소 1개 필요하다`() {
        assertThatThrownBy {
            Question(
                id = 1L,
                partId = 10L,
                category = QuestionCategory.PART,
                content = "파트 질문",
                sortOrder = 0,
                requirementIds = emptyList(),
            )
        }.isInstanceOf(QuestionInvalidException::class.java)
    }

    @Test
    fun `CULTURE 질문은 요구조건이 최소 1개 필요하다`() {
        assertThatThrownBy {
            Question(
                id = 1L,
                category = QuestionCategory.CULTURE,
                content = "컬쳐 질문",
                sortOrder = 0,
                requirementIds = emptyList(),
            )
        }.isInstanceOf(QuestionInvalidException::class.java)
    }

    @Test
    fun `GLOBAL 질문은 요구조건이 없어도 된다`() {
        assertThatCode {
            Question(
                id = 1L,
                category = QuestionCategory.GLOBAL,
                content = "전역 질문",
                sortOrder = 0,
                requirementIds = emptyList(),
            )
        }.doesNotThrowAnyException()
    }
}
