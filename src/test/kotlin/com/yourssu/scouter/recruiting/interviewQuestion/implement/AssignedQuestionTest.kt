package com.yourssu.scouter.recruiting.interviewQuestion.implement

import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionInvalidException
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

@Suppress("NonAsciiCharacters")
class AssignedQuestionTest {

    @Test
    fun `카탈로그 질문은 sourceQuestionId가 필요하다`() {
        assertThatThrownBy {
            AssignedQuestion(
                assignedInterviewerUserId = 1L,
                applicantId = 1L,
                sourceQuestionId = null,
                content = null,
                category = AssignedQuestionCategory.CULTURE,
                sortOrder = 0,
            )
        }.isInstanceOf(AssignedQuestionInvalidException::class.java)
    }

    @Test
    fun `전역 질문은 content를 직접 가질 수 없다`() {
        assertThatThrownBy {
            AssignedQuestion(
                assignedInterviewerUserId = 1L,
                applicantId = 1L,
                sourceQuestionId = 1L,
                content = "직접 수정한 질문",
                category = AssignedQuestionCategory.GLOBAL,
                sortOrder = 0,
            )
        }.isInstanceOf(AssignedQuestionInvalidException::class.java)
    }

    @Test
    fun `컬쳐 질문은 content를 직접 가질 수 없다`() {
        assertThatThrownBy {
            AssignedQuestion(
                assignedInterviewerUserId = 1L,
                applicantId = 1L,
                sourceQuestionId = 1L,
                content = "직접 수정한 질문",
                category = AssignedQuestionCategory.CULTURE,
                sortOrder = 0,
            )
        }.isInstanceOf(AssignedQuestionInvalidException::class.java)
    }

    @Test
    fun `파트 질문은 카탈로그 기반이라 content를 직접 가질 수 없다`() {
        assertThatThrownBy {
            AssignedQuestion(
                assignedInterviewerUserId = 1L,
                applicantId = 1L,
                sourceQuestionId = 1L,
                content = "직접 수정한 질문",
                category = AssignedQuestionCategory.PART,
                sortOrder = 0,
            )
        }.isInstanceOf(AssignedQuestionInvalidException::class.java)
    }

    @Test
    fun `개인 질문은 content가 필요하다`() {
        assertThatThrownBy {
            AssignedQuestion(
                assignedInterviewerUserId = 1L,
                applicantId = 1L,
                sourceQuestionId = null,
                content = null,
                category = AssignedQuestionCategory.PERSONAL,
                sortOrder = 0,
                requirementIds = listOf(1L),
            )
        }.isInstanceOf(AssignedQuestionInvalidException::class.java)
    }

    @Test
    fun `카탈로그 질문은 요구조건을 직접 가질 수 없다`() {
        assertThatThrownBy {
            AssignedQuestion(
                assignedInterviewerUserId = 1L,
                applicantId = 1L,
                sourceQuestionId = 1L,
                content = null,
                category = AssignedQuestionCategory.PART,
                sortOrder = 0,
                requirementIds = listOf(1L),
            )
        }.isInstanceOf(AssignedQuestionInvalidException::class.java)
    }

    @Test
    fun `개인 질문은 요구조건이 최소 1개 필요하다`() {
        assertThatThrownBy {
            AssignedQuestion(
                assignedInterviewerUserId = 1L,
                applicantId = 1L,
                sourceQuestionId = null,
                content = "개인 질문",
                category = AssignedQuestionCategory.PERSONAL,
                sortOrder = 0,
                requirementIds = emptyList(),
            )
        }.isInstanceOf(AssignedQuestionInvalidException::class.java)
    }
}
