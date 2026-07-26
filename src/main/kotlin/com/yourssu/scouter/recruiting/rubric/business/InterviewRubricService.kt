package com.yourssu.scouter.recruiting.rubric.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.recruiting.rubric.business.dto.InterviewRubricResult
import com.yourssu.scouter.recruiting.rubric.business.dto.UpdateInterviewRubricCommand
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricReader
import com.yourssu.scouter.recruiting.rubric.implement.InterviewRubricWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewRubricService(
    private val interviewRubricReader: InterviewRubricReader,
    private val interviewRubricWriter: InterviewRubricWriter,
    private val partReader: PartReader,
) {

    @Transactional(readOnly = true)
    fun readByPartIdAndSemester(partId: Long, semester: String): InterviewRubricResult {
        partReader.readById(partId)

        return InterviewRubricResult.from(interviewRubricReader.getByPartIdAndSemester(partId, semester))
    }

    @Transactional
    fun upsert(command: UpdateInterviewRubricCommand): InterviewRubricResult {
        partReader.readById(command.partId)

        val existing = interviewRubricReader.findByPartIdAndSemester(command.partId, command.semester)
        existing?.validateEditable()

        val saved = interviewRubricWriter.save(
            command.toDomain(
                existingId = existing?.id,
                isLocked = existing?.isLocked ?: false,
            ),
        )

        return InterviewRubricResult.from(saved)
    }
}
