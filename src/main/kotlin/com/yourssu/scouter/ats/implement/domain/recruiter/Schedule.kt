package com.yourssu.scouter.ats.implement.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.common.implement.domain.part.Part
import java.time.LocalDateTime

data class Schedule(
    val id: Long?,
    val applicant: Applicant,
    val interviewTime: LocalDateTime,
    val part: Part
) {
    companion object {
        fun create(
            applicant: Applicant,
            interviewTime: LocalDateTime,
            part: Part
        ): Schedule {
            validateInterviewTime(interviewTime)
            return Schedule(null, applicant, interviewTime, part)
        }

        private fun validateInterviewTime(time: LocalDateTime) {
            require(time.isAfter(LocalDateTime.now())) {"면접 시간은 현재 시간 이후여야 합니다: $time"}
        }
    }

    fun getDuplicateKey(): String = "${part.id}-$interviewTime"
}