package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.masterdata.semester.implement.fixture.SemesterFixtureBuilder
import com.yourssu.scouter.member.core.fixture.MemberFixtureBuilder
import com.yourssu.scouter.member.core.implement.MemberReader
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantReader
import com.yourssu.scouter.recruiting.applicant.implement.fixture.ApplicantFixtureBuilder
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionCommand
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.SaveAssignedQuestionsCommand
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestion
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionFactory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionValidator
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionResolver
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartCultureSelectionWriter
import com.yourssu.scouter.recruiting.interviewQuestion.implement.PartQuestionCatalogEditor
import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionWriter
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricLockPolicy
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.implement.exception.QuestionInvalidException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever
import java.time.Instant

@Suppress("NonAsciiCharacters")
class AssignedQuestionServiceUpsertTest {

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
    private lateinit var interviewRubricReader: InterviewRubricReader
    private lateinit var partReader: PartReader
    private lateinit var assignedQuestionService: AssignedQuestionService

    private val applicantId = 1L
    private val partId = 10L
    private val semesterId = 1L
    private val semester: Semester = SemesterFixtureBuilder().id(semesterId).build()
    private val interviewerMemberId = 100L
    private val cultureQuestionId1 = 1L
    private val cultureQuestionId2 = 2L
    private val partQuestionId = 7L

    private val partCatalogQuestion =
        Question(partQuestionId, partId, semesterId, QuestionCategory.PART, "파트 질문", 0, requirementIds = listOf(401L))

    /** 카탈로그 질문 저장소를 흉내내, 이번 요청에서 새로 만들어진 질문도 id로 다시 조회된다. */
    private val catalogById = mutableMapOf<Long, Question>()
    private var nextCatalogId = 99L

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
        interviewRubricReader = mock(InterviewRubricReader::class.java)
        partReader = mock(PartReader::class.java)

        assignedQuestionService = AssignedQuestionService(
            assignedQuestionReader,
            assignedQuestionWriter,
            AssignedQuestionFactory(assignedQuestionReader),
            AssignedQuestionValidator(),
            AssignedQuestionViewAssembler(questionReader, memberReader, interviewRequirementLookup),
            PartQuestionCatalogEditor(questionReader, questionWriter, assignedQuestionWriter),
            PartCultureSelectionResolver(partCultureSelectionReader, partCultureSelectionWriter),
            partCultureSelectionReader,
            questionReader,
            applicantReader,
            memberReader,
            interviewEvaluationReader,
            InterviewRubricLockPolicy(interviewRubricReader, partReader),
        )

        whenever(interviewEvaluationReader.existsByApplicantId(any())).thenReturn(false)
        whenever(interviewRubricReader.findByPartIdAndSemester(partId, semester)).thenReturn(null)
        whenever(partCultureSelectionReader.readSelectedQuestionIds(any(), any()))
            .thenReturn(setOf(cultureQuestionId1, cultureQuestionId2))
        whenever(applicantReader.readById(applicantId)).thenReturn(
            ApplicantFixtureBuilder()
                .id(applicantId)
                .part(PartFixtureBuilder().id(partId).build())
                .applicationSemester(semester)
                .build(),
        )
        whenever(interviewRequirementLookup.findAllByPartIdAndSemester(any(), any())).thenReturn(emptyList())
        whenever(memberReader.readById(interviewerMemberId)).thenReturn(
            MemberFixtureBuilder().id(interviewerMemberId).build(),
        )

        catalogById.clear()
        nextCatalogId = 99L
        listOf(
            Question(cultureQuestionId1, null, semesterId, QuestionCategory.CULTURE, "컬쳐1", 1, requirementIds = listOf(1L)),
            Question(cultureQuestionId2, null, semesterId, QuestionCategory.CULTURE, "컬쳐2", 2, requirementIds = listOf(1L)),
            partCatalogQuestion,
        ).forEach { catalogById[it.id!!] = it }

        whenever(questionReader.readAllByIdIn(any())).thenAnswer { invocation ->
            @Suppress("UNCHECKED_CAST")
            (invocation.arguments[0] as Collection<Long>).mapNotNull(catalogById::get)
        }
        whenever(questionWriter.save(any())).thenAnswer { invocation ->
            withId(invocation.arguments[0] as Question, nextCatalogId++).also { catalogById[it.id!!] = it }
        }
        whenever(assignedQuestionWriter.replaceAll(any(), any())).thenReturn(emptyList())
    }

    @Test
    fun `파트가 잠기지 않았으면 요청에서 빠진 기존 PART 질문을 카탈로그와 모든 지원자 인스턴스에서 삭제한다`() {
        whenever(questionReader.readAllByPartIdAndSemesterId(partId, semesterId)).thenReturn(listOf(partCatalogQuestion))

        assignedQuestionService.upsert(applicantId, SaveAssignedQuestionsCommand(selectedCultureCommands()))

        verify(assignedQuestionWriter).deleteAllBySourceQuestionIdIn(listOf(partQuestionId))
        verify(questionWriter).deleteAllByIdIn(setOf(partQuestionId))
    }

    @Test
    fun `이번 요청에서 새로 만든 PART 질문은 삭제 대상이 되지 않는다`() {
        val newQuestionId = 99L
        whenever(questionReader.readAllByPartIdAndSemesterId(partId, semesterId))
            .thenReturn(emptyList(), listOf(savedPartQuestion(newQuestionId, "새 파트 질문", sortOrder = 0)))

        assignedQuestionService.upsert(
            applicantId,
            SaveAssignedQuestionsCommand(selectedCultureCommands() + newPartCommand("새 파트 질문")),
        )

        verify(assignedQuestionWriter, never()).deleteAllBySourceQuestionIdIn(any())
        verify(questionWriter, never()).deleteAllByIdIn(any())
    }

    @Test
    fun `신규 PART 질문의 sortOrder는 기존 카탈로그 개수부터 이어진다`() {
        whenever(questionReader.readAllByPartIdAndSemesterId(partId, semesterId)).thenReturn(listOf(partCatalogQuestion))

        assignedQuestionService.upsert(
            applicantId,
            SaveAssignedQuestionsCommand(
                selectedCultureCommands() +
                    existingPartCommand() +
                    newPartCommand("새 파트 질문 1") +
                    newPartCommand("새 파트 질문 2"),
            ),
        )

        val captor = argumentCaptor<Question>()
        verify(questionWriter, times(2)).save(captor.capture())
        assertThat(captor.allValues.map { it.sortOrder }).containsExactly(1, 2)
    }

    @Test
    fun `신규 PART 질문에 content가 없으면 예외를 발생시킨다`() {
        assertThatThrownBy {
            assignedQuestionService.upsert(
                applicantId,
                SaveAssignedQuestionsCommand(selectedCultureCommands() + newPartCommand(content = null)),
            )
        }.isInstanceOf(QuestionInvalidException::class.java)
    }

    @Test
    fun `CULTURE 질문의 content를 변경하려 하면 예외를 발생시킨다`() {
        val commands = listOf(
            cultureCommand(cultureQuestionId1, isSelected = true, content = "컬쳐 질문을 바꿔본다"),
            cultureCommand(cultureQuestionId2, isSelected = true),
        )

        assertThatThrownBy { assignedQuestionService.upsert(applicantId, SaveAssignedQuestionsCommand(commands)) }
            .isInstanceOf(QuestionInvalidException::class.java)
    }

    @Test
    fun `저장되는 인스턴스는 CULTURE 선택 여부를 갖지 않고 PERSONAL 선택 여부는 요청값을 유지한다`() {
        assignedQuestionService.upsert(
            applicantId,
            SaveAssignedQuestionsCommand(selectedCultureCommands() + personalCommand(isSelected = true)),
        )

        val saved = capturedReplaceAll()
        assertThat(saved.filter { it.category == AssignedQuestionCategory.CULTURE })
            .hasSize(2)
            .allMatch { it.isSelected == null }
        assertThat(saved.single { it.category == AssignedQuestionCategory.PERSONAL }.isSelected).isTrue()
        assertThat(saved.map { it.sortOrder }).containsExactly(0, 1, 2)
    }

    @Test
    fun `CULTURE 선택이 그대로면 파트 선택 상태를 다시 쓰지 않는다`() {
        assignedQuestionService.upsert(applicantId, SaveAssignedQuestionsCommand(selectedCultureCommands()))

        verify(partCultureSelectionWriter, never()).replaceSelection(any(), any(), any())
    }

    @Test
    fun `CULTURE 선택이 바뀌면 파트와 학기 단위 선택 상태를 새 선택으로 교체한다`() {
        val cultureQuestionId3 = 3L
        catalogById[cultureQuestionId3] =
            Question(cultureQuestionId3, null, semesterId, QuestionCategory.CULTURE, "컬쳐3", 3, requirementIds = listOf(1L))

        assignedQuestionService.upsert(
            applicantId,
            SaveAssignedQuestionsCommand(
                listOf(
                    cultureCommand(cultureQuestionId1, isSelected = true),
                    cultureCommand(cultureQuestionId2, isSelected = false),
                    cultureCommand(cultureQuestionId3, isSelected = true),
                ),
            ),
        )

        val captor = argumentCaptor<List<Long>>()
        verify(partCultureSelectionWriter).replaceSelection(any(), any(), captor.capture())
        assertThat(captor.firstValue).containsExactlyInAnyOrder(cultureQuestionId1, cultureQuestionId3)
    }

    @Test
    fun `파트가 잠기면 신규 PART 질문 생성 요청을 무시한다`() {
        lockPart()

        assignedQuestionService.upsert(
            applicantId,
            SaveAssignedQuestionsCommand(selectedCultureCommands() + newPartCommand("새 파트 질문")),
        )

        verify(questionWriter, never()).save(any())
        assertThat(capturedReplaceAll().map { it.sortOrder }).containsExactly(0, 1)
    }

    @Test
    fun `파트가 잠기면 PART 질문의 content와 요구조건 변경 요청을 무시한다`() {
        lockPart()

        assignedQuestionService.upsert(
            applicantId,
            SaveAssignedQuestionsCommand(
                selectedCultureCommands() +
                    existingPartCommand(content = "바꿔본다", requirementIds = listOf(402L)),
            ),
        )

        verify(questionWriter, never()).update(any())
    }

    @Test
    fun `파트가 잠기면 CULTURE 선택 변경 요청을 무시하고 기존 선택을 반환한다`() {
        lockPart()
        whenever(questionReader.readAllBySemesterId(semesterId)).thenReturn(catalogById.values.toList())

        val result = assignedQuestionService.upsert(
            applicantId,
            SaveAssignedQuestionsCommand(
                listOf(
                    cultureCommand(cultureQuestionId1, isSelected = false),
                    cultureCommand(cultureQuestionId2, isSelected = false),
                ),
            ),
        )

        verify(partCultureSelectionWriter, never()).replaceSelection(any(), any(), any())
        assertThat(result.questions.filter { it.category == AssignedQuestionCategory.CULTURE })
            .hasSize(2)
            .allMatch { it.isSelected == true }
    }

    private fun lockPart() {
        whenever(interviewRubricReader.findByPartIdAndSemester(partId, semester)).thenReturn(
            InterviewRubric(
                id = 1L,
                partId = partId,
                semester = semester,
                deadline = Instant.now(),
                isLocked = true,
                items = emptyList(),
            ),
        )
    }

    private fun capturedReplaceAll(): List<AssignedQuestion> {
        val captor = argumentCaptor<List<AssignedQuestion>>()
        verify(assignedQuestionWriter).replaceAll(any(), captor.capture())
        return captor.firstValue
    }

    private fun selectedCultureCommands() = listOf(
        cultureCommand(cultureQuestionId1, isSelected = true),
        cultureCommand(cultureQuestionId2, isSelected = true),
    )

    private fun cultureCommand(sourceQuestionId: Long, isSelected: Boolean, content: String? = null) =
        SaveAssignedQuestionCommand(
            assignedMemberId = interviewerMemberId,
            sourceQuestionId = sourceQuestionId,
            content = content,
            category = AssignedQuestionCategory.CULTURE,
            isSelected = isSelected,
        )

    private fun existingPartCommand(content: String? = null, requirementIds: List<Long>? = null) =
        SaveAssignedQuestionCommand(
            assignedMemberId = interviewerMemberId,
            sourceQuestionId = partQuestionId,
            content = content,
            category = AssignedQuestionCategory.PART,
            requirementIds = requirementIds,
        )

    private fun newPartCommand(content: String?) = SaveAssignedQuestionCommand(
        assignedMemberId = interviewerMemberId,
        sourceQuestionId = null,
        content = content,
        category = AssignedQuestionCategory.PART,
        requirementIds = listOf(501L),
    )

    private fun personalCommand(isSelected: Boolean) = SaveAssignedQuestionCommand(
        assignedMemberId = interviewerMemberId,
        sourceQuestionId = null,
        content = "개인 질문",
        category = AssignedQuestionCategory.PERSONAL,
        isSelected = isSelected,
        requirementIds = listOf(301L),
    )

    private fun savedPartQuestion(id: Long, content: String, sortOrder: Int) =
        Question(id, partId, semesterId, QuestionCategory.PART, content, sortOrder, requirementIds = listOf(501L))

    private fun withId(question: Question, id: Long) = Question(
        id = id,
        partId = question.partId,
        semesterId = question.semesterId,
        category = question.category,
        content = question.content,
        sortOrder = question.sortOrder,
        requirementIds = question.requirementIds,
    )
}
