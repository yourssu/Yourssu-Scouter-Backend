package com.yourssu.scouter.recruiting.rubric.business

import com.yourssu.scouter.recruiting.rubric.business.dto.UpdateRubricItemCommand

import com.yourssu.scouter.recruiting.rubric.business.dto.DocumentRubricsResult

import com.yourssu.scouter.recruiting.rubric.business.dto.DocumentSectionDto

import com.yourssu.scouter.masterdata.part.implement.PartReader
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

    fun readByPartId(partId: Long): DocumentRubricsResult {
        partReader.readById(partId)

        val sections = documentSectionReader.readAllByPartId(partId)
        if (sections.isEmpty()) {
            return DocumentRubricsResult(isLocked = false, sections = emptyList())
        }

        // updateRubrics와 동일한 정책. 파트의 문항 중 하나라도 참조하는 서류 평가가 있으면 전체를 수정할 수 없다.
        val isLocked = documentEvaluationReader.existsBySectionIdIn(sections.mapNotNull { it.id })

        return DocumentRubricsResult(isLocked = isLocked, sections = sections.map(DocumentSectionDto::from))
    }

    @Transactional
    fun updateRubrics(partId: Long, items: List<UpdateRubricItemCommand>) {
        partReader.readById(partId)
        require(items.all { it.maxScore > 0 }) { "평가 항목의 배점은 1점 이상 할당 되어야 합니다." }
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

    // local, dev 프로필 전용 QA API. 파트의 모든 서류 평가 문항 배점을 0으로 초기화한다.
    @Transactional
    fun resetMaxScoresToZero(partId: Long) {
        partReader.readById(partId)

        val sections = documentSectionReader.readAllByPartId(partId)
        if (sections.isEmpty()) {
            return
        }

        val sectionIds = sections.mapNotNull { it.id }
        if (documentEvaluationReader.existsBySectionIdIn(sectionIds)) {
            throw RubricLockedException("해당 파트에 서류 평가가 존재해 배점을 초기화할 수 없습니다. 평가 데이터를 먼저 삭제해 주세요.")
        }

        val reset = sections.map { section ->
            DocumentSection(
                id = section.id,
                partId = section.partId,
                question = section.question,
                maxScore = 0,
                criterionDetail = section.criterionDetail,
            )
        }
        documentSectionWriter.writeAll(reset)
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
