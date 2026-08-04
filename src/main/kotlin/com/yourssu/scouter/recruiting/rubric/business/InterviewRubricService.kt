package com.yourssu.scouter.recruiting.rubric.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationItem
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.rubric.business.dto.InterviewRubricResult
import com.yourssu.scouter.recruiting.rubric.business.dto.UpdateInterviewRubricCommand
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubric
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.implement.exception.RubricLockedException
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
        existing?.validateEditable()

        if (existing != null && existing.items.isNotEmpty()) {
            val itemIds = existing.items.mapNotNull { it.id }
            if (interviewEvaluationReader.existsByInterviewEvaluationItemIdIn(itemIds)) {
                throw RubricLockedException("해당 파트에 면접 평가(임시저장 포함)가 존재해 수정 불가")
            }
        }

        val domain = command.toDomain(
            existingId = existing?.id,
            isLocked = existing?.isLocked ?: false,
        )
        domain.validateTotalScore()

        val saved = interviewRubricWriter.save(domain)

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
}
