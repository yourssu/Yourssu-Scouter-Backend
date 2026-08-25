package com.yourssu.scouter.recruiting.rubric.business

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.part.implement.fixture.PartFixtureBuilder
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluationReader
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSection
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionReader
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionWriter
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Suppress("NonAsciiCharacters")
class DocumentSectionServiceTest {

    private lateinit var documentSectionReader: DocumentSectionReader
    private lateinit var documentSectionWriter: DocumentSectionWriter
    private lateinit var documentEvaluationReader: DocumentEvaluationReader
    private lateinit var partReader: PartReader
    private lateinit var documentSectionService: DocumentSectionService

    private val partId = 10L

    @BeforeEach
    fun setUp() {
        documentSectionReader = mock(DocumentSectionReader::class.java)
        documentSectionWriter = mock(DocumentSectionWriter::class.java)
        documentEvaluationReader = mock(DocumentEvaluationReader::class.java)
        partReader = mock(PartReader::class.java)

        documentSectionService = DocumentSectionService(
            documentSectionReader,
            documentSectionWriter,
            documentEvaluationReader,
            partReader,
        )

        whenever(partReader.readById(partId)).thenReturn(PartFixtureBuilder().id(partId).build())
    }

    @Test
    fun `참조하는 서류 평가가 없으면 isLocked는 false다`() {
        whenever(documentSectionReader.readAllByPartId(partId)).thenReturn(listOf(section(1L), section(2L)))
        whenever(documentEvaluationReader.existsBySectionIdIn(listOf(1L, 2L))).thenReturn(false)

        val result = documentSectionService.readByPartId(partId)

        assertThat(result.isLocked).isFalse
        assertThat(result.sections).extracting<Long> { it.sectionId }.containsExactly(1L, 2L)
    }

    @Test
    fun `문항 중 하나라도 참조하는 서류 평가가 있으면 isLocked는 true다`() {
        whenever(documentSectionReader.readAllByPartId(partId)).thenReturn(listOf(section(1L), section(2L)))
        whenever(documentEvaluationReader.existsBySectionIdIn(listOf(1L, 2L))).thenReturn(true)

        val result = documentSectionService.readByPartId(partId)

        assertThat(result.isLocked).isTrue
        assertThat(result.sections).hasSize(2)
    }

    @Test
    fun `문항이 없으면 평가 조회 없이 잠기지 않은 빈 목록을 반환한다`() {
        whenever(documentSectionReader.readAllByPartId(partId)).thenReturn(emptyList())

        val result = documentSectionService.readByPartId(partId)

        assertThat(result.isLocked).isFalse
        assertThat(result.sections).isEmpty()
        verify(documentEvaluationReader, never()).existsBySectionIdIn(any())
        verify(documentSectionWriter, never()).writeAll(any())
    }

    private fun section(id: Long): DocumentSection = DocumentSection(
        id = id,
        partId = partId,
        question = "질문 $id",
        maxScore = 50,
        criterionDetail = "지표 $id",
    )
}
