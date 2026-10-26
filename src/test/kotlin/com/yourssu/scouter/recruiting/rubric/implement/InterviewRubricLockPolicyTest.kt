package com.yourssu.scouter.recruiting.rubric.implement

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.recruiting.support.implement.exception.RubricLockedException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import java.time.Instant

@Suppress("NonAsciiCharacters")
class InterviewRubricLockPolicyTest {

    private lateinit var interviewRubricReader: InterviewRubricReader
    private lateinit var partReader: PartReader
    private lateinit var policy: InterviewRubricLockPolicy

    private val semester = Semester.of("2026-1")
    private val backendPartId = 1L
    private val frontendPartId = 2L

    @BeforeEach
    fun setUp() {
        interviewRubricReader = mock(InterviewRubricReader::class.java)
        partReader = mock(PartReader::class.java)
        policy = InterviewRubricLockPolicy(interviewRubricReader, partReader)

        whenever(partReader.readAll()).thenReturn(
            listOf(
                PartFixtureBuilder().id(backendPartId).name("백엔드").build(),
                PartFixtureBuilder().id(frontendPartId).name("프론트엔드").build(),
            ),
        )
    }

    @Test
    fun `루브릭이 아직 없으면 잠기지 않은 것으로 본다`() {
        whenever(interviewRubricReader.findByPartIdAndSemester(backendPartId, semester)).thenReturn(null)

        assertThat(policy.isLocked(backendPartId, semester)).isFalse
        assertThatCode { policy.validateUnlocked(backendPartId, semester) }.doesNotThrowAnyException()
    }

    @Test
    fun `루브릭의 is_locked 값을 그대로 따른다`() {
        whenever(interviewRubricReader.findByPartIdAndSemester(backendPartId, semester)).thenReturn(rubric(backendPartId, isLocked = false))
        whenever(interviewRubricReader.findByPartIdAndSemester(frontendPartId, semester)).thenReturn(rubric(frontendPartId, isLocked = true))

        assertThat(policy.isLocked(backendPartId, semester)).isFalse
        assertThat(policy.isLocked(frontendPartId, semester)).isTrue
    }

    @Test
    fun `잠긴 루브릭을 수정하려 하면 예외가 발생한다`() {
        whenever(interviewRubricReader.findByPartIdAndSemester(backendPartId, semester)).thenReturn(rubric(backendPartId, isLocked = true))

        assertThatThrownBy { policy.validateUnlocked(backendPartId, semester) }
            .isInstanceOf(RubricLockedException::class.java)
    }

    @Test
    fun `한 파트라도 잠기면 학기 전체가 잠긴 것으로 보고 잠긴 파트 이름을 알려준다`() {
        whenever(interviewRubricReader.findByPartIdAndSemester(backendPartId, semester)).thenReturn(null)
        whenever(interviewRubricReader.findByPartIdAndSemester(frontendPartId, semester)).thenReturn(rubric(frontendPartId, isLocked = true))

        assertThat(policy.isAnyPartLocked(semester)).isTrue
        assertThatThrownBy { policy.validateNoPartLocked(semester) }
            .isInstanceOf(RubricLockedException::class.java)
            .hasMessageContaining("프론트엔드")
    }

    @Test
    fun `모든 파트가 잠기지 않았으면 학기 전체 수정이 가능하다`() {
        whenever(interviewRubricReader.findByPartIdAndSemester(backendPartId, semester)).thenReturn(rubric(backendPartId, isLocked = false))
        whenever(interviewRubricReader.findByPartIdAndSemester(frontendPartId, semester)).thenReturn(null)

        assertThat(policy.isAnyPartLocked(semester)).isFalse
        assertThatCode { policy.validateNoPartLocked(semester) }.doesNotThrowAnyException()
    }

    private fun rubric(partId: Long, isLocked: Boolean) = InterviewRubric(
        id = partId * 100,
        partId = partId,
        semester = semester,
        deadline = Instant.now(),
        isLocked = isLocked,
        items = emptyList(),
    )
}
