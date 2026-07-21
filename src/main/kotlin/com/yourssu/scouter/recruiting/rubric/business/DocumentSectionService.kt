package com.yourssu.scouter.recruiting.rubric.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.recruiting.evaluation.implement.DocumentEvaluationReader
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSection
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionReader
import com.yourssu.scouter.recruiting.rubric.implement.DocumentSectionWriter
import com.yourssu.scouter.recruiting.support.implement.exception.DocumentSectionNotFoundException
import com.yourssu.scouter.recruiting.support.implement.exception.RubricLockedException
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

    // 구글폼 동기화 시 서술형 질문에서 문항을 자동 생성한다 [B1]. 질문 텍스트가 이미 있으면 기존 문항을 그대로 재사용하고,
    // 새 질문 텍스트만 배점 0 / 지표 빈 문자열로 생성한다 (배점·지표는 이후 PUT rubrics로 채운다).
    @Transactional
    fun syncSectionsFromQuestions(partId: Long, questions: Set<String>): Map<String, DocumentSection> {
        if (questions.isEmpty()) {
            return emptyMap()
        }

        val existingSections = documentSectionReader.readAllByPartId(partId)
        val existingByQuestion = existingSections.associateBy { it.question }

        val newQuestions = questions.filterNot { existingByQuestion.containsKey(it) }
        val createdSections = if (newQuestions.isEmpty()) {
            emptyList()
        } else {
            documentSectionWriter.writeAll(
                newQuestions.map { question ->
                    DocumentSection(partId = partId, question = question, maxScore = 0, criterionDetail = "")
                },
            )
        }

        return (existingSections + createdSections)
            .filter { it.question in questions }
            .associateBy { it.question }
    }
}
