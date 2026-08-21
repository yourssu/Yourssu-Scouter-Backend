package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.masterdata.semester.implement.SemesterReader
import com.yourssu.scouter.masterdata.semester.implement.fixture.SemesterFixtureBuilder
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewRequirementItemRequest
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewRequirementRequest
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementReader
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirementWriter
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class InterviewRequirementServiceTest {

    private lateinit var partInterviewRequirementReader: InterviewRequirementReader
    private lateinit var partInterviewRequirementWriter: InterviewRequirementWriter
    private lateinit var partReader: PartReader
    private lateinit var semesterReader: SemesterReader
    private lateinit var interviewRubricReader: InterviewRubricReader
    private lateinit var interviewEvaluationReader: InterviewEvaluationReader
    private lateinit var interviewRubricWriter: InterviewRubricWriter
    private lateinit var service: InterviewRequirementService

    private val partId = 10L
    private val semester = SemesterFixtureBuilder().id(1L).build()

    @BeforeEach
    fun setUp() {
        partInterviewRequirementReader = mock(InterviewRequirementReader::class.java)
        partInterviewRequirementWriter = mock(InterviewRequirementWriter::class.java)
        partReader = mock(PartReader::class.java)
        semesterReader = mock(SemesterReader::class.java)
        interviewRubricReader = mock(InterviewRubricReader::class.java)
        interviewEvaluationReader = mock(InterviewEvaluationReader::class.java)
        interviewRubricWriter = mock(InterviewRubricWriter::class.java)

        service = InterviewRequirementService(
            partInterviewRequirementReader,
            partInterviewRequirementWriter,
            partReader,
            semesterReader,
            interviewRubricReader,
            interviewEvaluationReader,
            interviewRubricWriter,
        )

        whenever(partReader.readById(partId)).thenReturn(PartFixtureBuilder().id(partId).build())
        whenever(semesterReader.read(semester)).thenReturn(semester)
        whenever(interviewRubricReader.findByPartIdAndSemester(partId, semester)).thenReturn(null)
        whenever(partInterviewRequirementReader.readAllApplicableByPartIdAndSemester(any(), any()))
            .thenReturn(emptyList())
        whenever(partInterviewRequirementWriter.saveAll(any(), any(), any())).thenReturn(emptyList())
    }

    @Test
    fun `다른 카테고리에 섞여 들어온 전역 요구조건 id는 저장 대상에서 조용히 제외한다`() {
        // given: 전역 CULTURE 요구조건(101)이 있고, 이 파트가 소유한 TEAM 요구조건은 없다
        val globalCultureId = 101L
        whenever(partInterviewRequirementReader.readAllByPartIdAndSemester(partId, semester))
            .thenReturn(emptyList())

        // team-fit 페이지에서 새 TEAM 요구조건을 추가하면서, 건드리지 않은 culture 목록은
        // 화면에 표시됐던 전역 항목 id(101)를 그대로 echo해서 보낸다
        val request = UpdateInterviewRequirementRequest(
            culture = listOf(UpdateInterviewRequirementItemRequest(globalCultureId, "전역 문화 적합성")),
            team = listOf(UpdateInterviewRequirementItemRequest(null, "새 팀워크 요구조건")),
            job = emptyList(),
            other = emptyList(),
        )

        // when / then: "해당 범위에 존재하지 않는 요구조건 ID" 예외 없이 저장되어야 한다
        assertThatCode { service.saveAll(partId, semester, request) }.doesNotThrowAnyException()

        val captor = argumentCaptor<List<InterviewRequirement>>()
        verify(partInterviewRequirementWriter).saveAll(captor.capture(), any(), any())

        val saved = captor.firstValue
        // 전역 culture 항목(101)은 이 파트 저장 대상에 포함되지 않아야 한다
        assertThatCode {
            require(saved.none { it.id == globalCultureId }) { "전역 요구조건이 파트 범위 저장에 포함됨" }
        }.doesNotThrowAnyException()
        require(saved.any { it.rubricType == RubricGroupType.TEAM && it.content == "새 팀워크 요구조건" })
    }
}
