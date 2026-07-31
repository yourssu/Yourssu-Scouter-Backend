package com.yourssu.scouter.recruiting.interviewQuestion.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.recruiting.interviewQuestion.business.dto.QuestionDto
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionReader
import org.springframework.stereotype.Service

@Service
class QuestionService(
    private val questionReader: QuestionReader,
    private val partReader: PartReader,
) {

    fun readAll(partId: Long): List<QuestionDto> {
        partReader.readById(partId)

        return questionReader.readAll()
            .filter { it.partId == null || it.partId == partId }
            .map(QuestionDto::from)
    }
}
