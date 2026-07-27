package com.yourssu.scouter.recruiting.question.business

import com.yourssu.scouter.common.part.implement.PartReader
import com.yourssu.scouter.recruiting.question.business.dto.FixedQuestionDto
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionReader
import org.springframework.stereotype.Service

@Service
class FixedQuestionService(
    private val fixedQuestionReader: FixedQuestionReader,
    private val partReader: PartReader,
) {

    fun readAll(partId: Long): List<FixedQuestionDto> {
        partReader.readById(partId)

        return fixedQuestionReader.readAll().map(FixedQuestionDto::from)
    }
}
