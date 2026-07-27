package com.yourssu.scouter.recruiting.question.business

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.support.implement.exception.UserNotFoundException
import com.yourssu.scouter.auth.user.implement.TokenInfo
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserInfo
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.applicant.implement.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.recruiting.question.business.dto.SaveQuestionnaireCommand
import com.yourssu.scouter.recruiting.question.business.dto.SaveQuestionnaireQuestionCommand
import com.yourssu.scouter.recruiting.question.implement.FixedQuestion
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionCategory
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionReader
import com.yourssu.scouter.recruiting.question.implement.QuestionGroup
import com.yourssu.scouter.recruiting.question.implement.Questionnaire
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireQuestion
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireValidator
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireReader
import com.yourssu.scouter.recruiting.question.implement.QuestionnaireWriter
import com.yourssu.scouter.recruiting.support.implement.exception.ApplicantNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.time.Instant

@Suppress("NonAsciiCharacters")
class QuestionnaireServiceTest {

    private lateinit var questionnaireReader: QuestionnaireReader
    private lateinit var questionnaireWriter: QuestionnaireWriter
    private lateinit var fixedQuestionReader: FixedQuestionReader
    private lateinit var applicantReader: ApplicantReader
    private lateinit var userReader: UserReader
    private lateinit var questionnaireService: QuestionnaireService

    private val applicantId = 1L
    private val interviewerUserId = 10L

    // 검증 로직 자체는 QuestionnaireValidatorTest/QuestionnaireQuestionTest에서 다루므로, 여기서는 순수 로직을 그대로 사용한다.
    private val questionnaireValidator = QuestionnaireValidator()

    private fun user(id: Long) = User(
        id = id,
        userInfo = UserInfo(
            name = "면접관",
            email = "interviewer@example.com",
            profileImageUrl = "https://example.com/profile.png",
            oauthId = "oauth-$id",
            oauth2Type = OAuth2Type.GOOGLE,
        ),
        tokenInfo = TokenInfo(
            tokenPrefix = "Bearer",
            accessToken = "access-token",
            refreshToken = "refresh-token",
            accessTokenExpirationDateTime = Instant.now().plusSeconds(3600),
        ),
    )

    private fun cultureFitFixed(sourceQuestionId: Long) = SaveQuestionnaireQuestionCommand(
        group = QuestionGroup.CULTURE_FIT,
        sourceQuestionId = sourceQuestionId,
        content = null,
    )

    private fun freeQuestion(group: QuestionGroup, content: String?) = SaveQuestionnaireQuestionCommand(
        group = group,
        sourceQuestionId = null,
        content = content,
    )

    @BeforeEach
    fun setUp() {
        questionnaireReader = mock(QuestionnaireReader::class.java)
        questionnaireWriter = mock(QuestionnaireWriter::class.java)
        fixedQuestionReader = mock(FixedQuestionReader::class.java)
        applicantReader = mock(ApplicantReader::class.java)
        userReader = mock(UserReader::class.java)

        questionnaireService = QuestionnaireService(
            questionnaireReader,
            questionnaireWriter,
            fixedQuestionReader,
            questionnaireValidator,
            applicantReader,
            userReader,
        )

        whenever(applicantReader.readById(applicantId)).thenReturn(ApplicantFixtureBuilder().id(applicantId).build())
        whenever(userReader.readById(interviewerUserId)).thenReturn(user(interviewerUserId))
    }

    @Nested
    @DisplayName("readByApplicantId 메서드는")
    inner class ReadByApplicantIdTests {

        @Test
        fun `존재하지 않는 지원자면 예외를 발생시킨다`() {
            whenever(applicantReader.readById(999L)).thenThrow(ApplicantNotFoundException("지정한 지원자를 찾을 수 없습니다."))

            assertThatThrownBy { questionnaireService.readByApplicantId(999L) }
                .isInstanceOf(ApplicantNotFoundException::class.java)
        }

        @Test
        fun `아직 질문지가 없으면 빈 응답을 반환한다`() {
            whenever(questionnaireReader.readByApplicantId(applicantId)).thenReturn(null)
            whenever(questionnaireReader.readQuestionsByApplicantId(applicantId)).thenReturn(emptyList())
            whenever(fixedQuestionReader.readAllByIdIn(any())).thenReturn(emptyList())

            val result = questionnaireService.readByApplicantId(applicantId)

            assertThat(result.assignedInterviewerUserId).isNull()
            assertThat(result.questions).isEmpty()
        }
    }

    @Nested
    @DisplayName("upsert 메서드는")
    inner class UpsertTests {

        @Test
        fun `존재하지 않는 면접관이면 예외를 발생시킨다`() {
            whenever(userReader.readById(999L)).thenThrow(UserNotFoundException("지정한 사용자를 찾을 수 없습니다."))
            val command = SaveQuestionnaireCommand(
                assignedInterviewerUserId = 999L,
                questions = listOf(cultureFitFixed(1L), cultureFitFixed(2L)),
            )

            assertThatThrownBy { questionnaireService.upsert(applicantId, command) }
                .isInstanceOf(UserNotFoundException::class.java)
        }

        @Test
        fun `검증에 실패하면 저장하지 않고 예외를 전파한다`() {
            whenever(fixedQuestionReader.readAllByIdIn(any())).thenReturn(emptyList())
            val command = SaveQuestionnaireCommand(
                assignedInterviewerUserId = interviewerUserId,
                questions = listOf(cultureFitFixed(1L)),
            )

            assertThatThrownBy { questionnaireService.upsert(applicantId, command) }
                .isInstanceOf(RuntimeException::class.java)
        }

        @Test
        fun `검증을 통과하면 질문지를 저장하고 결과를 반환한다`() {
            val fixedQuestions = listOf(
                FixedQuestion(1L, FixedQuestionCategory.CULTURE_FIT, "고정질문1", 1),
                FixedQuestion(2L, FixedQuestionCategory.CULTURE_FIT, "고정질문2", 2),
            )
            whenever(fixedQuestionReader.readAllByIdIn(any())).thenReturn(fixedQuestions)

            val command = SaveQuestionnaireCommand(
                assignedInterviewerUserId = interviewerUserId,
                questions = listOf(cultureFitFixed(1L), cultureFitFixed(2L), freeQuestion(QuestionGroup.PERSONAL, "자유 문항")),
            )

            val savedQuestions = listOf(
                QuestionnaireQuestion(1L, applicantId, QuestionGroup.CULTURE_FIT, 1L, null, 0),
                QuestionnaireQuestion(2L, applicantId, QuestionGroup.CULTURE_FIT, 2L, null, 1),
                QuestionnaireQuestion(3L, applicantId, QuestionGroup.PERSONAL, null, "자유 문항", 2),
            )
            whenever(questionnaireWriter.upsert(any<Questionnaire>(), any())).thenReturn(savedQuestions)

            val result = questionnaireService.upsert(applicantId, command)

            assertThat(result.assignedInterviewerUserId).isEqualTo(interviewerUserId)
            assertThat(result.questions).hasSize(3)
            assertThat(result.questions[0].content).isEqualTo("고정질문1")
            assertThat(result.questions[2].content).isEqualTo("자유 문항")
        }
    }
}
