package com.yourssu.scouter.recruiting.interview.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.recruiting.interview.business.dto.InterviewScriptDto
import com.yourssu.scouter.recruiting.interview.implement.InterviewScript
import com.yourssu.scouter.recruiting.interview.implement.InterviewScriptReader
import com.yourssu.scouter.recruiting.interview.implement.InterviewScriptWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InterviewScriptService(
    private val interviewScriptReader: InterviewScriptReader,
    private val interviewScriptWriter: InterviewScriptWriter,
    private val partReader: PartReader,
) {

    fun readByPartId(partId: Long): InterviewScriptDto {
        partReader.readById(partId)

        return InterviewScriptDto.from(interviewScriptReader.readByPartId(partId))
    }

    @Transactional
    fun upsert(partId: Long, opening: String, closing: String) {
        partReader.readById(partId)

        interviewScriptWriter.upsert(InterviewScript(partId, opening, closing))
    }
}
