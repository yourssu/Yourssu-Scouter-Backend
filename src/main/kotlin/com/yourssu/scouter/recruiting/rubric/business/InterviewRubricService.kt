package com.yourssu.scouter.recruiting.rubric.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewEvaluationReader
import com.yourssu.scouter.recruiting.rubric.business.dto.InterviewRubricResult
import com.yourssu.scouter.recruiting.rubric.business.dto.UpdateInterviewRubricCommand
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import com.yourssu.scouter.recruiting.support.implement.exception.RubricLockedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewRubricService(
    private val interviewRubricReader: InterviewRubricReader,
    private val interviewRubricWriter: InterviewRubricWriter,
    private val interviewEvaluationReader: InterviewEvaluationReader,
    private val partReader: PartReader,
) {

    @Transactional(readOnly = true)
    fun readByPartIdAndSemester(partId: Long, semester: String): InterviewRubricResult {
        partReader.readById(partId)

        return InterviewRubricResult.from(interviewRubricReader.getByPartIdAndSemester(partId, Semester.of(semester)))
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
}
