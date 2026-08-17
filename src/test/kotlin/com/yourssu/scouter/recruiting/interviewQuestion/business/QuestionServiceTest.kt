package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.semester.implement.SemesterReader
import com.yourssu.scouter.masterdata.semester.implement.fixture.SemesterFixtureBuilder
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementReader
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.CreateQuestionsRequest
import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionWriter
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class QuestionServiceTest {

    private lateinit var questionReader: QuestionReader
    private lateinit var questionWriter: QuestionWriter
    private lateinit var requirementReader: InterviewRequirementReader
    private lateinit var semesterReader: SemesterReader
    private lateinit var questionService: QuestionService

    private val semesterId = 1L
    private val semesterString = "2026-1"

    @BeforeEach
    fun setUp() {
        questionReader = mock(QuestionReader::class.java)
        questionWriter = mock(QuestionWriter::class.java)
        requirementReader = mock(InterviewRequirementReader::class.java)
        semesterReader = mock(SemesterReader::class.java)

        questionService = QuestionService(
            questionReader = questionReader,
            partReader = mock(PartReader::class.java),
            semesterReader = semesterReader,
            questionWriter = questionWriter,
            requirementReader = requirementReader,
        )

        whenever(semesterReader.readByString(semesterString)).thenReturn(SemesterFixtureBuilder().id(semesterId).build())
    }

    @Test
    fun `INTRO, OUTRO, CULTURE 질문을 각각의 카테고리로 생성한다`() {
        whenever(questionReader.readAll()).thenReturn(emptyList())
        whenever(questionReader.readAllBySemesterId(semesterId)).thenReturn(emptyList())
        whenever(requirementReader.readAllByIdIn(listOf(1L))).thenReturn(
            listOf(
                InterviewRequirement(
                    id = 1L,
                    partId = null,
                    semesterId = 1L,
                    rubricType = RubricGroupType.CULTURE,
                    content = "컬쳐 요구조건",
                ),
            ),
        )
        whenever(questionWriter.save(any())).thenReturn(
            Question(1L, null, null, QuestionCategory.INTRO, "자기소개", 1),
            Question(2L, null, null, QuestionCategory.OUTRO, "마지막 한마디", 1),
            Question(3L, null, semesterId, QuestionCategory.CULTURE, "컬쳐 질문", 1, listOf(1L)),
        )

        questionService.upsert(
            semesterString,
            CreateQuestionsRequest(
                intro = listOf(CreateQuestionsRequest.Item(content = "자기소개", sortOrder = 1)),
                outro = listOf(CreateQuestionsRequest.Item(content = "마지막 한마디", sortOrder = 1)),
                culture = listOf(
                    CreateQuestionsRequest.Item(
                        content = "컬쳐 질문",
                        sortOrder = 1,
                        requirementIds = listOf(1L),
                    ),
                ),
            ),
        )

        val captor = argumentCaptor<Question>()
        org.mockito.kotlin.verify(questionWriter, org.mockito.kotlin.times(3)).save(captor.capture())

        assertThat(captor.allValues.map { it.category })
            .containsExactly(QuestionCategory.INTRO, QuestionCategory.OUTRO, QuestionCategory.CULTURE)
    }
}

