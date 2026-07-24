package com.yourssu.scouter.recruiting.interview.application.dto

import com.yourssu.scouter.recruiting.interview.business.dto.InterviewScriptDto

data class ReadInterviewScriptResponse(
    val opening: String?,
    val closing: String?,
) {
    companion object {
        fun from(dto: InterviewScriptDto): ReadInterviewScriptResponse = ReadInterviewScriptResponse(
            opening = dto.opening,
            closing = dto.closing,
        )
    }
}
