package com.yourssu.scouter.recruiting.interview.business.dto

import com.yourssu.scouter.recruiting.interview.implement.InterviewScript

data class InterviewScriptDto(
    val opening: String?,
    val closing: String?,
) {
    companion object {
        fun from(script: InterviewScript?): InterviewScriptDto = InterviewScriptDto(
            opening = script?.opening,
            closing = script?.closing,
        )
    }
}
