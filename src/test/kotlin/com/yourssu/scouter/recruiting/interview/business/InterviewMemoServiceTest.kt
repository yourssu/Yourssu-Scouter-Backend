package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.applicant.implement.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.recruiting.interview.business.dto.SaveInterviewMemoCommand
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemo
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemoReader
import com.yourssu.scouter.recruiting.interview.implement.InterviewMemoWriter
import com.yourssu.scouter.recruiting.question.implement.QuestionGroup
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestion
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireReader
import com.yourssu.scouter.recruiting.support.implement.exception.ApplicantNotFoundException
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionnaireQuestionNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class InterviewMemoServiceTest {

    private lateinit var applicantReader: ApplicantReader
    private lateinit var questionnaireReader: QuestionnaireReader
    private lateinit var interviewMemoReader: InterviewMemoReader
    private lateinit var interviewMemoWriter: InterviewMemoWriter
    private lateinit var interviewMemoService: InterviewMemoService

    private val applicantId = 1L

    private fun question(id: Long, sortOrder: Int) = QuestionnaireQuestion(
        id = id,
        questionnaireId = applicantId,
        group = QuestionGroup.PERSONAL,
        sourceQuestionId = null,
        content = "질문 $id",
        sortOrder = sortOrder,
    )

    @BeforeEach
    fun setUp() {
        applicantReader = mock(ApplicantReader::class.java)
        questionnaireReader = mock(QuestionnaireReader::class.java)
        interviewMemoReader = mock(InterviewMemoReader::class.java)
        interviewMemoWriter = mock(InterviewMemoWriter::class.java)

        interviewMemoService = InterviewMemoService(
            applicantReader,
            questionnaireReader,
            interviewMemoReader,
            interviewMemoWriter,
        )

        whenever(applicantReader.readById(applicantId)).thenReturn(ApplicantFixtureBuilder().id(applicantId).build())
    }

    @Nested
    @DisplayName("readByApplicantId 메서드는")
    inner class ReadByApplicantIdTests {

        @Test
        fun `존재하지 않는 지원자면 예외를 발생시킨다`() {
            whenever(applicantReader.readById(999L)).thenThrow(ApplicantNotFoundException("지정한 지원자를 찾을 수 없습니다."))

            assertThatThrownBy { interviewMemoService.readByApplicantId(999L) }
                .isInstanceOf(ApplicantNotFoundException::class.java)
        }

        @Test
        fun `메모가 없는 질문은 빈 문자열을 반환한다`() {
            whenever(questionnaireReader.readQuestionsByApplicantId(applicantId))
                .thenReturn(listOf(question(1L, 0), question(2L, 1)))
            whenever(interviewMemoReader.readAllByQuestionnaireQuestionIdIn(listOf(1L, 2L)))
                .thenReturn(listOf(InterviewMemo(id = 10L, questionnaireQuestionId = 1L, memo = "메모1")))

            val result = interviewMemoService.readByApplicantId(applicantId)

            assertThat(result).hasSize(2)
            assertThat(result[0].questionnaireQuestionId).isEqualTo(1L)
            assertThat(result[0].memo).isEqualTo("메모1")
            assertThat(result[1].questionnaireQuestionId).isEqualTo(2L)
            assertThat(result[1].memo).isEmpty()
        }
    }

    @Nested
    @DisplayName("upsert 메서드는")
    inner class UpsertTests {

        @Test
        fun `해당 지원자의 질문지에 없는 문항이면 예외를 발생시킨다`() {
            whenever(questionnaireReader.readQuestionsByApplicantId(applicantId))
                .thenReturn(listOf(question(1L, 0)))

            val commands = listOf(SaveInterviewMemoCommand(questionnaireQuestionId = 999L, memo = "메모"))

            assertThatThrownBy { interviewMemoService.upsert(applicantId, commands) }
                .isInstanceOf(QuestionnaireQuestionNotFoundException::class.java)
        }

        @Test
        fun `검증을 통과하면 메모를 전체 치환하고 결과를 반환한다`() {
            whenever(questionnaireReader.readQuestionsByApplicantId(applicantId))
                .thenReturn(listOf(question(1L, 0), question(2L, 1)))

            val commands = listOf(
                SaveInterviewMemoCommand(questionnaireQuestionId = 1L, memo = "메모1"),
                SaveInterviewMemoCommand(questionnaireQuestionId = 2L, memo = "메모2"),
            )
            val saved = listOf(
                InterviewMemo(id = 10L, questionnaireQuestionId = 1L, memo = "메모1"),
                InterviewMemo(id = 11L, questionnaireQuestionId = 2L, memo = "메모2"),
            )
            whenever(interviewMemoWriter.replaceAll(any(), any())).thenReturn(saved)

            val result = interviewMemoService.upsert(applicantId, commands)

            assertThat(result).hasSize(2)
            assertThat(result[0].memo).isEqualTo("메모1")
            assertThat(result[1].memo).isEqualTo("메모2")
        }
    }
}
