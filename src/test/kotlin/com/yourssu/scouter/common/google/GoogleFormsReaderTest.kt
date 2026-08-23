package com.yourssu.scouter.common.google

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class GoogleFormsReaderTest {

    private val googleFormsClient: GoogleFormsClient = mock(GoogleFormsClient::class.java)
    private val googleFormsReader = GoogleFormsReader(googleFormsClient)

    @Test
    fun `응답 항목을 응답 Map 순서가 아니라 실제 폼 문항 순서로 정렬한다`() {
        // given: 폼 문항 순서는 이름 -> 자기소개(장문형) -> 취미 이지만,
        // 응답 Map은 반대 순서(취미 -> 자기소개 -> 이름)로 온다.
        val questions = GoogleFormQuestions(
            items = listOf(
                FormItem("item-name", "이름", QuestionItem(Question("q-name", TextQuestion(paragraph = false))), null),
                FormItem("item-intro", "자기소개", QuestionItem(Question("q-intro", TextQuestion(paragraph = true))), null),
                FormItem("item-hobby", "취미", QuestionItem(Question("q-hobby", TextQuestion(paragraph = false))), null),
            )
        )
        val responses = GoogleFormResponses(
            responses = listOf(
                GoogleUserResponse(
                    responseId = "response-1",
                    createTime = "2026-01-01T00:00:00Z",
                    respondentEmail = "test@example.com",
                    lastSubmittedTime = "2026-01-01T00:00:00Z",
                    answers = linkedMapOf(
                        "q-hobby" to Answer(TextAnswers(listOf(TextAnswer("등산")))),
                        "q-intro" to Answer(TextAnswers(listOf(TextAnswer("안녕하세요")))),
                        "q-name" to Answer(TextAnswers(listOf(TextAnswer("홍길동")))),
                    ),
                )
            )
        )
        whenever(googleFormsClient.getFormQuestions("Bearer token", "form-1")).thenReturn(questions)
        whenever(googleFormsClient.getFormResponses("Bearer token", "form-1")).thenReturn(responses)

        // when
        val userResponses = googleFormsReader.getUserResponses("Bearer token", "form-1")

        // then
        val responseItems = userResponses.single().responseItems
        assertThat(responseItems.map { it.question }).containsExactly("이름", "자기소개", "취미")
    }

    @Test
    fun `장문형(paragraph) 문항만 isDescriptive가 true다`() {
        val questions = GoogleFormQuestions(
            items = listOf(
                FormItem("item-name", "이름", QuestionItem(Question("q-name", TextQuestion(paragraph = false))), null),
                FormItem("item-intro", "자기소개", QuestionItem(Question("q-intro", TextQuestion(paragraph = true))), null),
            )
        )
        val responses = GoogleFormResponses(
            responses = listOf(
                GoogleUserResponse(
                    responseId = "response-1",
                    createTime = "2026-01-01T00:00:00Z",
                    respondentEmail = null,
                    lastSubmittedTime = null,
                    answers = mapOf(
                        "q-name" to Answer(TextAnswers(listOf(TextAnswer("홍길동")))),
                        "q-intro" to Answer(TextAnswers(listOf(TextAnswer("안녕하세요")))),
                    ),
                )
            )
        )
        whenever(googleFormsClient.getFormQuestions("Bearer token", "form-1")).thenReturn(questions)
        whenever(googleFormsClient.getFormResponses("Bearer token", "form-1")).thenReturn(responses)

        val responseItems = googleFormsReader.getUserResponses("Bearer token", "form-1").single().responseItems

        assertThat(responseItems.find { it.question == "이름" }?.isDescriptive).isFalse()
        assertThat(responseItems.find { it.question == "자기소개" }?.isDescriptive).isTrue()
    }
}
