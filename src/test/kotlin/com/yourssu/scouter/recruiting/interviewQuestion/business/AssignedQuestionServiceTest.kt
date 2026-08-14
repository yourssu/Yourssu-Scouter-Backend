package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserInfo
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.auth.user.implement.TokenInfo
import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.masterdata.semester.implement.fixture.SemesterFixtureBuilder
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.applicant.implement.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionsCommand
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestion
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionValidator
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionWriter
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.business.EvaluatorInfo
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class AssignedQuestionServiceTest {

    private lateinit var assignedQuestionReader: AssignedQuestionReader
    private lateinit var assignedQuestionWriter: AssignedQuestionWriter
    private lateinit var questionReader: QuestionReader
    private lateinit var questionWriter: QuestionWriter
    private lateinit var applicantReader: ApplicantReader
    private lateinit var userReader: UserReader
    private lateinit var evaluatorDirectory: EvaluatorDirectory
    private lateinit var interviewRequirementLookup: InterviewRequirementLookup
    private lateinit var assignedQuestionService: AssignedQuestionService

    private val applicantId = 1L
    private val partId = 10L

    @BeforeEach
    fun setUp() {
        assignedQuestionReader = mock(AssignedQuestionReader::class.java)
        assignedQuestionWriter = mock(AssignedQuestionWriter::class.java)
        questionReader = mock(QuestionReader::class.java)
        questionWriter = mock(QuestionWriter::class.java)
        applicantReader = mock(ApplicantReader::class.java)
        userReader = mock(UserReader::class.java)
        evaluatorDirectory = mock(EvaluatorDirectory::class.java)
        interviewRequirementLookup = mock(InterviewRequirementLookup::class.java)

        assignedQuestionService = AssignedQuestionService(
            assignedQuestionReader,
            assignedQuestionWriter,
            questionReader,
            questionWriter,
            AssignedQuestionValidator(),
            applicantReader,
            userReader,
            evaluatorDirectory,
            interviewRequirementLookup,
        )

        whenever(applicantReader.readById(applicantId)).thenReturn(
            ApplicantFixtureBuilder()
                .id(applicantId)
                .part(PartFixtureBuilder().id(partId).build())
                .applicationSemester(SemesterFixtureBuilder().id(1L).build())
                .build(),
        )
        whenever(interviewRequirementLookup.findAllByPartIdAndSemester(any(), any())).thenReturn(emptyList())
        whenever(userReader.readAllByIds(any())).thenReturn(emptyList())
    }

    @Test
    fun `저장된 질문이 없으면 고정 질문과 파트 질문과 컬쳐 질문 옵션 4개를 반환한다`() {
        whenever(assignedQuestionReader.readAllByApplicantId(applicantId)).thenReturn(emptyList())
        whenever(questionReader.readAll()).thenReturn(
            listOf(
                Question(1L, null, QuestionCategory.GLOBAL, "자기소개", 1),
                Question(2L, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
                Question(3L, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
                Question(4L, null, QuestionCategory.CULTURE, "컬쳐3", 3, requirementIds = listOf(1L)),
                Question(5L, null, QuestionCategory.CULTURE, "컬쳐4", 4, requirementIds = listOf(1L)),
                Question(7L, partId, QuestionCategory.PART, "파트 질문", 1, requirementIds = listOf(901L)),
            ),
        )

        val result = assignedQuestionService.readByApplicantId(applicantId)

        assertThat(result.questions).hasSize(6)
        assertThat(result.questions.count { it.category == AssignedQuestionCategory.GLOBAL }).isEqualTo(1)
        assertThat(result.questions.count { it.category == AssignedQuestionCategory.PART }).isEqualTo(1)
        assertThat(result.questions.count { it.category == AssignedQuestionCategory.CULTURE }).isEqualTo(4)
        assertThat(result.questions.filter { it.category == AssignedQuestionCategory.CULTURE })
            .allSatisfy { assertThat(it.isSelected).isFalse() }
        assertThat(result.questions.filter { it.category != AssignedQuestionCategory.CULTURE })
            .allSatisfy { assertThat(it.isSelected).isNull() }
        assertThat(result.questions).allSatisfy {
            assertThat(it.id).isNull()
            assertThat(it.assignedInterviewerName).isNull()
        }
    }

    @Test
    fun `저장된 질문은 배정된 면접관 영어 닉네임을 반환한다`() {
        val interviewerUserId = 100L
        val interviewerEmail = "interviewer@yourssu.com"
        val sourceQuestions = listOf(
            Question(1L, null, QuestionCategory.GLOBAL, "자기소개", 1),
        )
        val savedQuestions = listOf(
            AssignedQuestion(
                id = 11L,
                assignedInterviewerUserId = interviewerUserId,
                applicantId = applicantId,
                sourceQuestionId = 1L,
                content = null,
                category = AssignedQuestionCategory.GLOBAL,
                sortOrder = 0,
            ),
        )
        whenever(assignedQuestionReader.readAllByApplicantId(applicantId)).thenReturn(savedQuestions)
        whenever(questionReader.readAllByIdIn(listOf(1L))).thenReturn(sourceQuestions)
        whenever(userReader.readAllByIds(listOf(interviewerUserId))).thenReturn(
            listOf(user(interviewerUserId, "면접관", interviewerEmail)),
        )
        whenever(evaluatorDirectory.findEvaluatorInfo(interviewerEmail)).thenReturn(
            EvaluatorInfo(memberId = 1L, nicknameEnglish = "piki", partName = "Backend"),
        )

        val result = assignedQuestionService.readByApplicantId(applicantId)

        assertThat(result.questions.single().assignedInterviewerName).isEqualTo("piki")
    }

    @Test
    fun `저장한 컬쳐 질문 4개는 선택 여부와 함께 모두 반환한다`() {
        val interviewerUserId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(3L, null, QuestionCategory.CULTURE, "컬쳐3", 3, requirementIds = listOf(1L)),
            Question(4L, null, QuestionCategory.CULTURE, "컬쳐4", 4, requirementIds = listOf(1L)),
        )
        whenever(userReader.readById(interviewerUserId)).thenReturn(mock(User::class.java))
        whenever(questionReader.readAllByIdIn(listOf(1L, 2L, 3L, 4L))).thenReturn(sourceQuestions)

        val savedQuestions = listOf(
            assignedCultureQuestion(11L, interviewerUserId, 1L, isSelected = true, sortOrder = 0),
            assignedCultureQuestion(12L, interviewerUserId, 2L, isSelected = true, sortOrder = 1),
            assignedCultureQuestion(13L, interviewerUserId, 3L, isSelected = false, sortOrder = 2),
            assignedCultureQuestion(14L, interviewerUserId, 4L, isSelected = false, sortOrder = 3),
        )
        whenever(assignedQuestionWriter.replaceAll(any(), any())).thenReturn(savedQuestions)

        val command = SaveAssignedQuestionsCommand(
            questions = sourceQuestions.mapIndexed { index, question ->
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = question.id,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = index < 2,
                )
            },
        )

        val saved = assignedQuestionService.upsert(applicantId, command)

        assertThat(saved.questions).hasSize(4)
        assertThat(saved.questions.map { it.isSelected }).containsExactly(true, true, false, false)

        whenever(assignedQuestionReader.readAllByApplicantId(applicantId)).thenReturn(savedQuestions)

        val found = assignedQuestionService.readByApplicantId(applicantId)

        assertThat(found.questions).hasSize(4)
        assertThat(found.questions.map { it.sourceQuestionId }).containsExactly(1L, 2L, 3L, 4L)
        assertThat(found.questions.map { it.isSelected }).containsExactly(true, true, false, false)
    }

    @Test
    fun `파트 질문의 content 변경은 인스턴스가 아닌 카탈로그에 반영된다`() {
        val interviewerUserId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(7L, partId, QuestionCategory.PART, "카탈로그 파트 질문", 1, requirementIds = listOf(401L)),
        )
        whenever(userReader.readById(interviewerUserId)).thenReturn(mock(User::class.java))
        whenever(questionReader.readAllByIdIn(listOf(1L, 2L, 7L))).thenReturn(sourceQuestions)

        val savedQuestions = listOf(
            assignedCultureQuestion(11L, interviewerUserId, 1L, isSelected = true, sortOrder = 0),
            assignedCultureQuestion(12L, interviewerUserId, 2L, isSelected = true, sortOrder = 1),
            AssignedQuestion(
                id = 17L,
                assignedInterviewerUserId = interviewerUserId,
                applicantId = applicantId,
                sourceQuestionId = 7L,
                content = null,
                category = AssignedQuestionCategory.PART,
                sortOrder = 2,
            ),
        )
        whenever(assignedQuestionWriter.replaceAll(any(), any())).thenReturn(savedQuestions)

        val command = SaveAssignedQuestionsCommand(
            questions = listOf(
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 1L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 2L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 7L,
                    content = "새로 수정한 파트 질문",
                    category = AssignedQuestionCategory.PART,
                    requirementIds = listOf(402L),
                ),
            ),
        )

        val saved = assignedQuestionService.upsert(applicantId, command)

        assertThat(saved.questions.last().category).isEqualTo(AssignedQuestionCategory.PART)

        val captor = argumentCaptor<Question>()
        verify(questionWriter).update(captor.capture())
        assertThat(captor.firstValue.id).isEqualTo(7L)
        assertThat(captor.firstValue.content).isEqualTo("새로 수정한 파트 질문")
        assertThat(captor.firstValue.requirementIds).containsExactly(402L)
    }

    @Test
    fun `PART 질문에 요구조건이 없으면 예외를 발생시킨다`() {
        val interviewerUserId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(7L, partId, QuestionCategory.PART, "카탈로그 파트 질문", 1, requirementIds = listOf(401L)),
        )
        whenever(userReader.readById(interviewerUserId)).thenReturn(mock(User::class.java))
        whenever(questionReader.readAllByIdIn(listOf(1L, 2L, 7L))).thenReturn(sourceQuestions)

        val command = SaveAssignedQuestionsCommand(
            questions = listOf(
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 1L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 2L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 7L,
                    content = "파트 질문",
                    category = AssignedQuestionCategory.PART,
                    requirementIds = emptyList(),
                ),
            ),
        )

        assertThatThrownBy { assignedQuestionService.upsert(applicantId, command) }
            .isInstanceOf(QuestionInvalidException::class.java)
    }

    @Test
    fun `GLOBAL 질문에 요구조건을 지정하면 예외를 발생시킨다`() {
        val interviewerUserId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(3L, null, QuestionCategory.GLOBAL, "전역 질문", 1),
        )
        whenever(userReader.readById(interviewerUserId)).thenReturn(mock(User::class.java))
        whenever(questionReader.readAllByIdIn(listOf(1L, 2L, 3L))).thenReturn(sourceQuestions)

        val command = SaveAssignedQuestionsCommand(
            questions = listOf(
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 1L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 2L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 3L,
                    content = null,
                    category = AssignedQuestionCategory.GLOBAL,
                    requirementIds = listOf(1L),
                ),
            ),
        )

        assertThatThrownBy { assignedQuestionService.upsert(applicantId, command) }
            .isInstanceOf(QuestionInvalidException::class.java)
    }

    private fun assignedCultureQuestion(
        id: Long,
        interviewerUserId: Long,
        sourceQuestionId: Long,
        isSelected: Boolean,
        sortOrder: Int,
    ): AssignedQuestion {
        return AssignedQuestion(
            id = id,
            assignedInterviewerUserId = interviewerUserId,
            applicantId = applicantId,
            sourceQuestionId = sourceQuestionId,
            content = null,
            category = AssignedQuestionCategory.CULTURE,
            sortOrder = sortOrder,
            isSelected = isSelected,
        )
    }

    private fun user(id: Long, name: String, email: String): User {
        return User(
            id = id,
            userInfo = UserInfo(
                name = name,
                email = email,
                profileImageUrl = "",
                oauthId = "oauth-$id",
                oauth2Type = OAuth2Type.GOOGLE,
            ),
            tokenInfo = TokenInfo(
                tokenPrefix = "Bearer",
                accessToken = "access-token",
                refreshToken = "refresh-token",
                accessTokenExpiresIn = 3600L,
            ),
        )
    }
}
