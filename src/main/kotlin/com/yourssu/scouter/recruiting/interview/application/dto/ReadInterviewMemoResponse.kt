package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.interview.business.dto.InterviewMemoDto

data class ReadInterviewMemoResponse(
    val assignedQuestionId: Long,
    val memo: String,
) {
    companion object {
        fun from(dto: InterviewMemoDto): ReadInterviewMemoResponse = ReadInterviewMemoResponse(
            assignedQuestionId = dto.assignedQuestionId,
            memo = dto.memo,
        )
    }
}
