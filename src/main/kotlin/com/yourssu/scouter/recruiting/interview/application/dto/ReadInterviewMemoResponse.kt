package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.interview.business.dto.InterviewMemoDto

data class ReadInterviewMemoResponse(
    val questionnaireQuestionId: Long,
    val memo: String,
) {
    companion object {
        fun from(dto: InterviewMemoDto): ReadInterviewMemoResponse = ReadInterviewMemoResponse(
            questionnaireQuestionId = dto.questionnaireQuestionId,
            memo = dto.memo,
        )
    }
}
