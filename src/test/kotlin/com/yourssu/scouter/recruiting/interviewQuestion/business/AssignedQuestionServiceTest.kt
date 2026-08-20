package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.auth.authentication.implement.OAuth2Type
import com.yourssu.scouter.auth.user.implement.TokenInfo
import com.yourssu.scouter.auth.user.implement.User
import com.yourssu.scouter.auth.user.implement.UserInfo
import com.yourssu.scouter.auth.user.implement.UserReader
import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.masterdata.semester.implement.fixture.SemesterFixtureBuilder
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.applicant.implement.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.recruiting.evaluation.implement.FinalEvaluation
import com.yourssu.scouter.recruiting.evaluation.implement.FinalEvaluationReader
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionsCommand
import com.yourssu.scouter.recruiting.interviewQuestion.implement.*
import com.yourssu.scouter.recruiting.support.business.EvaluatorDirectory
import com.yourssu.scouter.recruiting.support.business.EvaluatorInfo
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionLockedException
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.mockito.Mockito.mock
import org.junit.jupiter.params.provider.EnumSource
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
    private lateinit var finalEvaluationReader: FinalEvaluationReader
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
        finalEvaluationReader = mock(FinalEvaluationReader::class.java)

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
            finalEvaluationReader,
        )

        whenever(finalEvaluationReader.readAllByApplicantId(applicantId)).thenReturn(emptyList())

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
        val semesterId = 1L
        whenever(assignedQuestionReader.readAllByApplicantId(applicantId)).thenReturn(emptyList())
        // INTRO/OUTRO: readAll()에서 반환
        whenever(questionReader.readAll()).thenReturn(
            listOf(
                Question(1L, null, null, QuestionCategory.INTRO, "자기소개", 1),
                Question(2L, null, null, QuestionCategory.OUTRO, "계획", 1),
            ),
        )
        // CULTURE: readAllBySemesterId()에서 반환
        whenever(questionReader.readAllBySemesterId(semesterId)).thenReturn(
            listOf(
                Question(3L, null, semesterId, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
                Question(4L, null, semesterId, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
                Question(5L, null, semesterId, QuestionCategory.CULTURE, "컬쳐3", 3, requirementIds = listOf(1L)),
                Question(6L, null, semesterId, QuestionCategory.CULTURE, "컬쳐4", 4, requirementIds = listOf(1L)),
            ),
        )
        // PART: readAllByPartIdAndSemesterId()에서 반환
        whenever(questionReader.readAllByPartIdAndSemesterId(partId, semesterId)).thenReturn(
            listOf(
                Question(7L, partId, semesterId, QuestionCategory.PART, "파트 질문", 1, requirementIds = listOf(901L)),
            ),
        )

        val result = assignedQuestionService.readByApplicantId(applicantId)

        assertThat(result.questions).hasSize(7)
        assertThat(result.questions.count { it.category == AssignedQuestionCategory.INTRO }).isEqualTo(1)
        assertThat(result.questions.count { it.category == AssignedQuestionCategory.OUTRO }).isEqualTo(1)
        assertThat(result.questions.count { it.category == AssignedQuestionCategory.PART }).isEqualTo(1)
        assertThat(result.questions.count { it.category == AssignedQuestionCategory.CULTURE }).isEqualTo(4)
        assertThat(result.questions.filter { it.category == AssignedQuestionCategory.CULTURE })
            .allSatisfy { assertThat(it.isSelected).isFalse() }
        assertThat(result.questions.filter { it.category != AssignedQuestionCategory.CULTURE })
            .allSatisfy { assertThat(it.isSelected).isNull() }
        assertThat(result.questions).allSatisfy {
            assertThat(it.id).isNull()
            assertThat(it.assignedInterviewerUserId).isNull()
            assertThat(it.assignedInterviewerName).isNull()
        }
    }

    @Test
    fun `저장된 질문은 배정된 면접관 영어 닉네임을 반환한다`() {
        val interviewerUserId = 100L
        val interviewerEmail = "interviewer@yourssu.com"
        val sourceQuestions = listOf(
            Question(1L, null, null, QuestionCategory.INTRO, "자기소개", 1),
        )
        val savedQuestions = listOf(
            AssignedQuestion(
                id = 11L,
                assignedInterviewerUserId = interviewerUserId,
                applicantId = applicantId,
                sourceQuestionId = 1L,
                content = null,
                category = AssignedQuestionCategory.INTRO,
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
        assertThat(result.questions.single().assignedInterviewerUserId).isEqualTo(interviewerUserId)
    }

    @Test
    fun `저장한 컬쳐 질문 4개는 선택 여부와 함께 모두 반환한다`() {
        val interviewerUserId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(3L, null, null, QuestionCategory.CULTURE, "컬쳐3", 3, requirementIds = listOf(1L)),
            Question(4L, null, null, QuestionCategory.CULTURE, "컬쳐4", 4, requirementIds = listOf(1L)),
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
            Question(1L, null, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(7L, partId, null, QuestionCategory.PART, "카탈로그 파트 질문", 1, requirementIds = listOf(401L)),
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
    fun `sourceQuestionId가 없는 신규 PART 질문은 카탈로그에 저장되고 그 id로 배정된다`() {
        val interviewerUserId = 100L
        val newQuestionId = 99L
        val cultureQuestions = listOf(
            Question(1L, null, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
        )

        whenever(userReader.readById(interviewerUserId)).thenReturn(mock(User::class.java))
        whenever(questionReader.readAllByPartIdAndSemesterId(partId, 1L)).thenReturn(emptyList())
        whenever(questionWriter.save(any())).thenAnswer { invocation ->
            val question = invocation.arguments[0] as Question
            Question(
                id = newQuestionId,
                partId = question.partId,
                semesterId = question.semesterId,
                category = question.category,
                content = question.content,
                sortOrder = question.sortOrder,
                requirementIds = question.requirementIds,
            )
        }

        val newPartQuestion = Question(newQuestionId, partId, 1L, QuestionCategory.PART, "새 파트 질문", 0, requirementIds = listOf(501L))
        whenever(questionReader.readAllByIdIn(listOf(1L, 2L, newQuestionId))).thenReturn(cultureQuestions + newPartQuestion)

        val savedAssignedQuestions = listOf(
            assignedCultureQuestion(21L, interviewerUserId, 1L, isSelected = true, sortOrder = 0),
            assignedCultureQuestion(22L, interviewerUserId, 2L, isSelected = true, sortOrder = 1),
            AssignedQuestion(
                id = 20L,
                assignedInterviewerUserId = interviewerUserId,
                applicantId = applicantId,
                sourceQuestionId = newQuestionId,
                content = null,
                category = AssignedQuestionCategory.PART,
                sortOrder = 2,
            ),
        )
        whenever(assignedQuestionWriter.replaceAll(any(), any())).thenReturn(savedAssignedQuestions)

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
                    sourceQuestionId = null,
                    content = "새 파트 질문",
                    category = AssignedQuestionCategory.PART,
                    requirementIds = listOf(501L),
                ),
            ),
        )

        val saved = assignedQuestionService.upsert(applicantId, command)

        assertThat(saved.questions.last().sourceQuestionId).isEqualTo(newQuestionId)

        val captor = argumentCaptor<Question>()
        verify(questionWriter).save(captor.capture())
        assertThat(captor.firstValue.id).isNull()
        assertThat(captor.firstValue.partId).isEqualTo(partId)
        assertThat(captor.firstValue.content).isEqualTo("새 파트 질문")
        assertThat(captor.firstValue.requirementIds).containsExactly(501L)
    }

    @Test
    fun `PART 질문에 요구조건이 없으면 예외를 발생시킨다`() {
        val interviewerUserId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(7L, partId, null, QuestionCategory.PART, "카탈로그 파트 질문", 1, requirementIds = listOf(401L)),
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

    @ParameterizedTest
    @EnumSource(value = AssignedQuestionCategory::class, names = ["INTRO", "OUTRO"])
    fun `INTRO, OUTRO 질문에 요구조건을 지정하면 예외를 발생시킨다`(
        targetCategory: AssignedQuestionCategory
    ) {
        val interviewerUserId = 100L
        val targetSourceQuestionId = 1L

        val sourceQuestions = listOf(
            Question(targetSourceQuestionId, null, null, QuestionCategory.valueOf(targetCategory.name), "필수 질문", 1),
            Question(
                targetSourceQuestionId + 1,
                null,
                null,
                QuestionCategory.CULTURE,
                "컬쳐 질문 1",
                2,
                requirementIds = listOf(1)
            ),
            Question(
                targetSourceQuestionId + 2,
                null,
                null,
                QuestionCategory.CULTURE,
                "컬쳐 질문 2",
                3,
                requirementIds = listOf(1)
            ),
        )

        whenever(userReader.readById(interviewerUserId)).thenReturn(mock(User::class.java))
        whenever(questionReader.readAllByIdIn(any())).thenReturn(sourceQuestions)

        val command = SaveAssignedQuestionsCommand(
            questions = listOf(
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = targetSourceQuestionId,
                    content = null,
                    category = targetCategory,
                    requirementIds = listOf(1L),
                ),
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = targetSourceQuestionId + 1,
                    content = null,
                    isSelected = true,
                    category = AssignedQuestionCategory.CULTURE,
                    requirementIds = listOf(1L),
                ),
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = targetSourceQuestionId + 2,
                    content = null,
                    isSelected = true,
                    category = AssignedQuestionCategory.CULTURE,
                    requirementIds = listOf(1L),
                ),
            ),
        )

        assertThatThrownBy { assignedQuestionService.upsert(applicantId, command) }
            .isInstanceOf(QuestionInvalidException::class.java)
    }

    @Test
    fun `면접 평가가 제출된 지원자의 질문지는 수정할 수 없다`() {
        val interviewerUserId = 100L
        whenever(finalEvaluationReader.readAllByApplicantId(applicantId)).thenReturn(
            listOf(
                FinalEvaluation(
                    evaluatorUserId = interviewerUserId,
                    interviewRubricId = 1L,
                    applicantId = applicantId,
                    score = 0,
                    submit = true,
                ),
            ),
        )

        val command = SaveAssignedQuestionsCommand(
            questions = listOf(
                SaveAssignedQuestionCommand(
                    assignedInterviewerUserId = interviewerUserId,
                    sourceQuestionId = 1L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
            ),
        )

        assertThatThrownBy { assignedQuestionService.upsert(applicantId, command) }
            .isInstanceOf(AssignedQuestionLockedException::class.java)
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
