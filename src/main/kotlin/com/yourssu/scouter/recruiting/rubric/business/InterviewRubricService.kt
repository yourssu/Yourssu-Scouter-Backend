package com.yourssu.scouter.recruiting.rubric.business

import com.yourssu.scouter.masterdata.part.implement.PartReader
import com.yourssu.scouter.masterdata.semester.implement.Semester
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationItem
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.rubric.business.dto.InterviewRubricResult
import com.yourssu.scouter.recruiting.rubric.business.dto.UpdateInterviewRubricCommand
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.implement.exception.RubricLockedException
import com.yourssu.scouter.recruiting.support.implement.exception.InterviewRubricNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant

@Service
class InterviewRubricService(
    private val interviewRubricReader: InterviewRubricReader,
    private val interviewRubricWriter: InterviewRubricWriter,
    private val interviewEvaluationReader: InterviewEvaluationReader,
    private val interviewRequirementLookup: InterviewRequirementLookup,
    private val partReader: PartReader,
) {

    @Transactional(readOnly = true)
    fun readByPartIdAndSemester(partId: Long, semester: String): InterviewRubricResult {
        partReader.readById(partId)

        val resolvedSemester = Semester.of(semester)
        val rubric = interviewRubricReader.findByPartIdAndSemester(partId, resolvedSemester)
            ?: buildTemplate(partId, resolvedSemester)

        return InterviewRubricResult.from(rubric)
    }

    private fun buildTemplate(partId: Long, semester: Semester): InterviewRubric {
        val requirements = interviewRequirementLookup.findAllByPartIdAndSemester(partId, semester)

        return InterviewRubric(
            id = null,
            partId = partId,
            semester = semester,
            deadline = Instant.now().plus(Duration.ofDays(365)),
            isLocked = false,
            items = requirements.map {
                InterviewEvaluationItem(
                    id = null,
                    keyword = it.content,
                    rubricType = it.rubricType,
                    maxScore = 0,
                )
            },
        )
    }

    @Transactional
    fun upsert(command: UpdateInterviewRubricCommand): InterviewRubricResult {
        partReader.readById(command.partId)

        val existing = interviewRubricReader.findByPartIdAndSemester(command.partId, Semester.of(command.semester))
            ?: throw InterviewRubricNotFoundException("해당 파트와 학기에 생성된 면접 루브릭이 없습니다. 면접 요구조건을 먼저 설정해 주세요. partId=${command.partId}, semester=${command.semester}")
        existing.validateEditable()

        val itemIds = existing.items.mapNotNull { it.id }
        if (itemIds.isNotEmpty()) {
            if (interviewEvaluationReader.existsByInterviewEvaluationItemIdIn(itemIds)) {
                throw RubricLockedException("해당 파트에 면접 평가가 존재해 수정 불가")
            }
        }

        val existingItemIds = existing.items.mapNotNull { it.id }.toSet()
        val requestItemIds = command.items.map { it.id }.toSet()
        if (existingItemIds != requestItemIds || command.items.size != requestItemIds.size) {
            throw IllegalArgumentException("요청된 평가 항목 ID 목록이 기존 루브릭의 평가 항목 ID 목록과 일치하지 않습니다. (항목을 추가하거나 삭제할 수 없습니다.)")
        }

        val existingItemsById = existing.items.associateBy { it.id!! }
        command.items.forEach { requestedItem ->
            val existingItem = existingItemsById[requestedItem.id]
                ?: throw IllegalArgumentException("존재하지 않는 면접 평가 항목입니다. itemId=${requestedItem.id}")
            require(existingItem.rubricType == requestedItem.group) {
                "요청 그룹과 면접 평가 항목의 실제 그룹이 일치하지 않습니다. itemId=${requestedItem.id}"
            }
        }

        val maxScoreMap = command.items.associate { it.id to it.maxScore }
        val updatedItems = existing.items.map { item ->
            InterviewEvaluationItem(
                id = item.id,
                interviewRequirementId = item.interviewRequirementId,
                keyword = item.keyword,
                rubricType = item.rubricType,
                maxScore = maxScoreMap[item.id!!] ?: item.maxScore
            )
        }

        val updated = existing.update(
            semester = existing.semester,
            deadline = command.deadline,
            items = updatedItems
        )
        updated.validateTotalScore()

        val saved = interviewRubricWriter.save(updated)

        return InterviewRubricResult.from(saved)
    }

    @Transactional
    fun updateDeadline(partId: Long, semester: String, deadline: Instant): InterviewRubricResult {
        partReader.readById(partId)

        val existing = interviewRubricReader.getByPartIdAndSemester(partId, Semester.of(semester))
        existing.validateEditable()

        val updated = existing.update(
            semester = existing.semester,
            deadline = deadline,
            items = existing.items,
        )

        return InterviewRubricResult.from(interviewRubricWriter.save(updated))
    }

    @Transactional(readOnly = true)
    fun readDeadline(partId: Long, semester: String): Instant {
        partReader.readById(partId)
        return interviewRubricReader
            .getByPartIdAndSemester(partId, Semester.of(semester))
            .deadline
    }
}
