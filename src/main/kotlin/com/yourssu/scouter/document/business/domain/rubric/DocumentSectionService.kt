package com.yourssu.scouter.document.business.domain.rubric

import com.yourssu.scouter.common.implement.domain.part.PartReader
import com.yourssu.scouter.document.implement.domain.evaluation.DocumentEvaluationReader
import com.yourssu.scouter.document.implement.domain.rubric.DocumentSection
import com.yourssu.scouter.document.implement.domain.rubric.DocumentSectionReader
import com.yourssu.scouter.document.implement.domain.rubric.DocumentSectionWriter
import com.yourssu.scouter.document.implement.support.exception.DocumentSectionNotFoundException
import com.yourssu.scouter.document.implement.support.exception.RubricLockedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DocumentSectionService(
    private val documentSectionReader: DocumentSectionReader,
    private val documentSectionWriter: DocumentSectionWriter,
    private val documentEvaluationReader: DocumentEvaluationReader,
    private val partReader: PartReader,
) {

    fun readByPartId(partId: Long): List<DocumentSectionDto> {
        partReader.readById(partId)

        return documentSectionReader.readAllByPartId(partId).map(DocumentSectionDto::from)
    }

    @Transactional
    fun updateRubrics(partId: Long, items: List<UpdateRubricItemCommand>) {
        partReader.readById(partId)
        require(items.sumOf { it.maxScore } == 100) { "배점 합계는 100이어야 합니다." }

        val partSections = documentSectionReader.readAllByPartId(partId).associateBy { it.id }
        val sectionIds = items.map { it.sectionId }
        sectionIds.forEach { sectionId ->
            if (sectionId !in partSections) {
                throw DocumentSectionNotFoundException("해당 파트에 존재하지 않는 문항입니다: $sectionId")
            }
        }

        if (documentEvaluationReader.existsBySectionIdIn(sectionIds)) {
            throw RubricLockedException("해당 문항을 참조하는 서류 평가가 존재해 수정할 수 없습니다.")
        }

        val updated = items.map { item ->
            val section: DocumentSection = partSections.getValue(item.sectionId)
            DocumentSection(
                id = section.id,
                partId = section.partId,
                question = section.question,
                maxScore = item.maxScore,
                criterionDetail = item.criterionDetail,
            )
        }
        documentSectionWriter.writeAll(updated)
    }
}
