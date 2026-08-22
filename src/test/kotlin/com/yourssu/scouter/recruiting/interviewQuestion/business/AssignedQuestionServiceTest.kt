package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.masterdata.semester.implement.fixture.SemesterFixtureBuilder
import com.yourssu.scouter.member.core.fixture.MemberFixtureBuilder
import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.applicant.implement.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionsCommand
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestion
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionValidator
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionWriter
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.implement.exception.AssignedQuestionLockedException
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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
    private lateinit var memberReader: MemberReader
    private lateinit var interviewRequirementLookup: InterviewRequirementLookup
    private lateinit var interviewEvaluationReader: InterviewEvaluationReader
    private lateinit var partCultureSelectionReader: PartCultureSelectionReader
    private lateinit var partCultureSelectionWriter: PartCultureSelectionWriter
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
        memberReader = mock(MemberReader::class.java)
        interviewRequirementLookup = mock(InterviewRequirementLookup::class.java)
        interviewEvaluationReader = mock(InterviewEvaluationReader::class.java)
        partCultureSelectionReader = mock(PartCultureSelectionReader::class.java)
        partCultureSelectionWriter = mock(PartCultureSelectionWriter::class.java)

        assignedQuestionService = AssignedQuestionService(
            assignedQuestionReader,
            assignedQuestionWriter,
            questionReader,
            questionWriter,
            AssignedQuestionValidator(),
            applicantReader,
            memberReader,
            interviewRequirementLookup,
            interviewEvaluationReader,
            partCultureSelectionReader,
            partCultureSelectionWriter,
        )

        whenever(interviewEvaluationReader.existsByApplicantId(any())).thenReturn(false)
        whenever(interviewEvaluationReader.existsByApplicantIdIn(any())).thenReturn(false)
        whenever(partCultureSelectionReader.readSelectedQuestionIds(any(), any())).thenReturn(emptySet())
        whenever(applicantReader.readById(applicantId)).thenReturn(
            ApplicantFixtureBuilder()
                .id(applicantId)
                .part(PartFixtureBuilder().id(partId).build())
                .applicationSemester(SemesterFixtureBuilder().id(1L).build())
                .build(),
        )
        whenever(interviewRequirementLookup.findAllByPartIdAndSemester(any(), any())).thenReturn(emptyList())
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
            assertThat(it.assignedMemberId).isNull()
            assertThat(it.assignedMemberName).isNull()
        }
    }

    @Test
    fun `저장된 질문은 배정된 면접관 영어 닉네임을 반환한다`() {
        val interviewerMemberId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, null, QuestionCategory.INTRO, "자기소개", 1),
        )
        val savedQuestions = listOf(
            AssignedQuestion(
                id = 11L,
                assignedMemberId = interviewerMemberId,
                applicantId = applicantId,
                sourceQuestionId = 1L,
                content = null,
                category = AssignedQuestionCategory.INTRO,
                sortOrder = 0,
            ),
        )
        whenever(assignedQuestionReader.readAllByApplicantId(applicantId)).thenReturn(savedQuestions)
        whenever(questionReader.readAllByIdIn(listOf(1L))).thenReturn(sourceQuestions)
        whenever(memberReader.readById(interviewerMemberId)).thenReturn(
            MemberFixtureBuilder().id(interviewerMemberId).name("면접관").build(),
        )

        val result = assignedQuestionService.readByApplicantId(applicantId)

        assertThat(result.questions.single().assignedMemberName).isEqualTo("piki")
        assertThat(result.questions.single().assignedMemberId).isEqualTo(interviewerMemberId)
    }

    @Test
    fun `저장한 컬쳐 질문 4개는 선택 여부와 함께 모두 반환한다`() {
        val interviewerMemberId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(3L, null, null, QuestionCategory.CULTURE, "컬쳐3", 3, requirementIds = listOf(1L)),
            Question(4L, null, null, QuestionCategory.CULTURE, "컬쳐4", 4, requirementIds = listOf(1L)),
        )
        whenever(memberReader.readById(interviewerMemberId)).thenReturn(
            MemberFixtureBuilder().id(interviewerMemberId).name("면접관").build(),
        )
        whenever(questionReader.readAllByIdIn(listOf(1L, 2L, 3L, 4L))).thenReturn(sourceQuestions)

        val savedQuestions = listOf(
            assignedCultureQuestion(11L, interviewerMemberId, 1L, isSelected = true, sortOrder = 0),
            assignedCultureQuestion(12L, interviewerMemberId, 2L, isSelected = true, sortOrder = 1),
            assignedCultureQuestion(13L, interviewerMemberId, 3L, isSelected = false, sortOrder = 2),
            assignedCultureQuestion(14L, interviewerMemberId, 4L, isSelected = false, sortOrder = 3),
        )
        whenever(assignedQuestionWriter.replaceAll(any(), any())).thenReturn(savedQuestions)

        val command = SaveAssignedQuestionsCommand(
            questions = sourceQuestions.mapIndexed { index, question ->
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
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
        whenever(partCultureSelectionReader.readSelectedQuestionIds(partId, 1L)).thenReturn(setOf(1L, 2L))

        val found = assignedQuestionService.readByApplicantId(applicantId)

        assertThat(found.questions).hasSize(4)
        assertThat(found.questions.map { it.sourceQuestionId }).containsExactly(1L, 2L, 3L, 4L)
        assertThat(found.questions.map { it.isSelected }).containsExactly(true, true, false, false)
    }

    @Test
    fun `선택되지 않아 인스턴스가 저장되지 않은 CULTURE 질문도 조회 시 카탈로그에서 채워져 함께 반환된다`() {
        val interviewerMemberId = 100L
        val semesterId = 1L

        // 이 지원자는 선택된 2개(1L, 2L)만 인스턴스로 저장되어 있고, 3L/4L은 저장된 적 없다.
        val savedQuestions = listOf(
            assignedCultureQuestion(11L, interviewerMemberId, 1L, isSelected = true, sortOrder = 0),
            assignedCultureQuestion(12L, interviewerMemberId, 2L, isSelected = true, sortOrder = 1),
        )
        whenever(assignedQuestionReader.readAllByApplicantId(applicantId)).thenReturn(savedQuestions)
        whenever(questionReader.readAllByIdIn(listOf(1L, 2L))).thenReturn(
            listOf(
                Question(1L, null, semesterId, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
                Question(2L, null, semesterId, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            ),
        )
        // 학기 전체 CULTURE 카탈로그는 4개
        whenever(questionReader.readAllBySemesterId(semesterId)).thenReturn(
            listOf(
                Question(1L, null, semesterId, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
                Question(2L, null, semesterId, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
                Question(3L, null, semesterId, QuestionCategory.CULTURE, "컬쳐3", 3, requirementIds = listOf(1L)),
                Question(4L, null, semesterId, QuestionCategory.CULTURE, "컬쳐4", 4, requirementIds = listOf(1L)),
            ),
        )
        whenever(memberReader.readById(interviewerMemberId)).thenReturn(
            MemberFixtureBuilder().id(interviewerMemberId).build(),
        )
        whenever(partCultureSelectionReader.readSelectedQuestionIds(partId, semesterId)).thenReturn(setOf(1L, 2L))

        val found = assignedQuestionService.readByApplicantId(applicantId)

        assertThat(found.questions).hasSize(4)
        assertThat(found.questions.map { it.sourceQuestionId }).containsExactlyInAnyOrder(1L, 2L, 3L, 4L)
        assertThat(found.questions.filter { it.sourceQuestionId in setOf(3L, 4L) })
            .allSatisfy {
                assertThat(it.id).isNull()
                assertThat(it.assignedMemberId).isNull()
                assertThat(it.isSelected).isFalse()
            }
    }

    @Test
    fun `다른 지원자의 저장으로 새로 생긴 PART 질문도 조회 시 카탈로그에서 채워져 함께 반환된다`() {
        val interviewerMemberId = 100L
        val semesterId = 1L

        // 이 지원자는 기존 PART 질문(7L)만 인스턴스로 저장되어 있고, 다른 지원자가 방금 만든 신규 PART 질문(8L)은 저장된 적 없다.
        val savedQuestions = listOf(
            AssignedQuestion(
                id = 31L,
                assignedMemberId = interviewerMemberId,
                applicantId = applicantId,
                sourceQuestionId = 7L,
                content = null,
                category = AssignedQuestionCategory.PART,
                sortOrder = 0,
            ),
        )
        whenever(assignedQuestionReader.readAllByApplicantId(applicantId)).thenReturn(savedQuestions)
        whenever(questionReader.readAllByIdIn(listOf(7L))).thenReturn(
            listOf(Question(7L, partId, semesterId, QuestionCategory.PART, "기존 파트 질문", 1, requirementIds = listOf(401L))),
        )
        whenever(questionReader.readAllByPartIdAndSemesterId(partId, semesterId)).thenReturn(
            listOf(
                Question(7L, partId, semesterId, QuestionCategory.PART, "기존 파트 질문", 1, requirementIds = listOf(401L)),
                Question(8L, partId, semesterId, QuestionCategory.PART, "신규 파트 질문", 2, requirementIds = listOf(402L)),
            ),
        )
        whenever(memberReader.readById(interviewerMemberId)).thenReturn(
            MemberFixtureBuilder().id(interviewerMemberId).build(),
        )

        val found = assignedQuestionService.readByApplicantId(applicantId)

        assertThat(found.questions).hasSize(2)
        assertThat(found.questions.map { it.sourceQuestionId }).containsExactlyInAnyOrder(7L, 8L)
        val newPartQuestionDto = found.questions.single { it.sourceQuestionId == 8L }
        assertThat(newPartQuestionDto.id).isNull()
        assertThat(newPartQuestionDto.assignedMemberId).isNull()
        assertThat(newPartQuestionDto.content).isEqualTo("신규 파트 질문")
    }

    @Test
    fun `파트 질문의 content 변경은 인스턴스가 아닌 카탈로그에 반영된다`() {
        val interviewerMemberId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(7L, partId, null, QuestionCategory.PART, "카탈로그 파트 질문", 1, requirementIds = listOf(401L)),
        )
        whenever(memberReader.readById(interviewerMemberId)).thenReturn(
            MemberFixtureBuilder().id(interviewerMemberId).build(),
        )
        whenever(questionReader.readAllByIdIn(listOf(1L, 2L, 7L))).thenReturn(sourceQuestions)

        val savedQuestions = listOf(
            assignedCultureQuestion(11L, interviewerMemberId, 1L, isSelected = true, sortOrder = 0),
            assignedCultureQuestion(12L, interviewerMemberId, 2L, isSelected = true, sortOrder = 1),
            AssignedQuestion(
                id = 17L,
                assignedMemberId = interviewerMemberId,
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
                    assignedMemberId = interviewerMemberId,
                    sourceQuestionId = 1L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
                    sourceQuestionId = 2L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
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
        val interviewerMemberId = 100L
        val newQuestionId = 99L
        val cultureQuestions = listOf(
            Question(1L, null, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
        )

        whenever(memberReader.readById(interviewerMemberId)).thenReturn(
            MemberFixtureBuilder().id(interviewerMemberId).build(),
        )
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
            assignedCultureQuestion(21L, interviewerMemberId, 1L, isSelected = true, sortOrder = 0),
            assignedCultureQuestion(22L, interviewerMemberId, 2L, isSelected = true, sortOrder = 1),
            AssignedQuestion(
                id = 20L,
                assignedMemberId = interviewerMemberId,
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
                    assignedMemberId = interviewerMemberId,
                    sourceQuestionId = 1L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
                    sourceQuestionId = 2L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
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
        val interviewerMemberId = 100L
        val sourceQuestions = listOf(
            Question(1L, null, null, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(2L, null, null, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            Question(7L, partId, null, QuestionCategory.PART, "카탈로그 파트 질문", 1, requirementIds = listOf(401L)),
        )
        whenever(memberReader.readById(interviewerMemberId)).thenReturn(
            MemberFixtureBuilder().id(interviewerMemberId).build(),
        )
        whenever(questionReader.readAllByIdIn(listOf(1L, 2L, 7L))).thenReturn(sourceQuestions)

        val command = SaveAssignedQuestionsCommand(
            questions = listOf(
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
                    sourceQuestionId = 1L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
                    sourceQuestionId = 2L,
                    content = null,
                    category = AssignedQuestionCategory.CULTURE,
                    isSelected = true,
                ),
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
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
        val interviewerMemberId = 100L
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

        whenever(memberReader.readById(interviewerMemberId)).thenReturn(
            MemberFixtureBuilder().id(interviewerMemberId).build(),
        )
        whenever(questionReader.readAllByIdIn(any())).thenReturn(sourceQuestions)

        val command = SaveAssignedQuestionsCommand(
            questions = listOf(
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
                    sourceQuestionId = targetSourceQuestionId,
                    content = null,
                    category = targetCategory,
                    requirementIds = listOf(1L),
                ),
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
                    sourceQuestionId = targetSourceQuestionId + 1,
                    content = null,
                    isSelected = true,
                    category = AssignedQuestionCategory.CULTURE,
                    requirementIds = listOf(1L),
                ),
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
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
    fun `제출된 면접 평가가 있으면 질문지 수정 시 예외를 발생시킨다`() {
        val interviewerMemberId = 100L
        whenever(interviewEvaluationReader.existsByApplicantId(applicantId)).thenReturn(true)

        val command = SaveAssignedQuestionsCommand(
            questions = listOf(
                SaveAssignedQuestionCommand(
                    assignedMemberId = interviewerMemberId,
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
        interviewerMemberId: Long,
        sourceQuestionId: Long,
        isSelected: Boolean,
        sortOrder: Int,
    ): AssignedQuestion {
        return AssignedQuestion(
            id = id,
            assignedMemberId = interviewerMemberId,
            applicantId = applicantId,
            sourceQuestionId = sourceQuestionId,
            content = null,
            category = AssignedQuestionCategory.CULTURE,
            sortOrder = sortOrder,
            isSelected = isSelected,
        )
    }
}
