package com.yourssu.scouter.recruiting.rubric.business

import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementProfile
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.Instant

@Suppress("NonAsciiCharacters")
class InterviewRubricServiceTest {

    private lateinit var interviewRubricReader: InterviewRubricReader
    private lateinit var interviewRubricWriter: InterviewRubricWriter
    private lateinit var interviewEvaluationReader: InterviewEvaluationReader
    private lateinit var interviewRequirementLookup: InterviewRequirementLookup
    private lateinit var partReader: PartReader
    private lateinit var interviewRubricService: InterviewRubricService

    private val partId = 10L
    private val semester = "2026-1"

    @BeforeEach
    fun setUp() {
        interviewRubricReader = mock(InterviewRubricReader::class.java)
        interviewRubricWriter = mock(InterviewRubricWriter::class.java)
        interviewEvaluationReader = mock(InterviewEvaluationReader::class.java)
        interviewRequirementLookup = mock(InterviewRequirementLookup::class.java)
        partReader = mock(PartReader::class.java)

        interviewRubricService = InterviewRubricService(
            interviewRubricReader,
            interviewRubricWriter,
            interviewEvaluationReader,
            interviewRequirementLookup,
            partReader,
        )

        whenever(partReader.readById(partId)).thenReturn(PartFixtureBuilder().id(partId).build())
    }

    @Test
    fun `저장된 루브릭이 없으면 요구조건 기반의 미저장 템플릿을 반환한다`() {
        whenever(interviewRubricReader.findByPartIdAndSemester(partId, Semester.of(semester))).thenReturn(null)
        whenever(interviewRequirementLookup.findAllByPartIdAndSemester(partId, Semester.of(semester))).thenReturn(
            listOf(
                InterviewRequirementProfile(1L, "주도성", RubricGroupType.CULTURE),
                InterviewRequirementProfile(2L, "팀워크", RubricGroupType.TEAM),
            ),
        )

        val result = interviewRubricService.readByPartIdAndSemester(partId, semester)

        assertThat(result.id).isEqualTo(0L)
        assertThat(result.items).hasSize(2)
        assertThat(result.items).allSatisfy { assertThat(it.maxScore).isEqualTo(0) }
        assertThat(result.items.map { it.keyword }).containsExactlyInAnyOrder("주도성", "팀워크")
    }

    @Test
    fun `저장된 루브릭이 있으면 그대로 반환하고 요구조건을 조회하지 않는다`() {
        val savedRubric = InterviewRubric(
            id = 100L,
            partId = partId,
            semester = Semester.of(semester),
            deadline = Instant.now(),
            isLocked = false,
            items = emptyList(),
        )
        whenever(interviewRubricReader.findByPartIdAndSemester(partId, Semester.of(semester))).thenReturn(savedRubric)

        val result = interviewRubricService.readByPartIdAndSemester(partId, semester)

        assertThat(result.id).isEqualTo(100L)
    }
}
