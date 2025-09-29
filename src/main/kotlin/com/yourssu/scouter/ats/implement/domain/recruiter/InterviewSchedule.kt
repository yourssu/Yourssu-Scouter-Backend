package com.yourssu.scouter.ats.implement.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.common.implement.domain.part.Part
import java.time.LocalDateTime

data class InterviewSchedule(
    val id: Long?,
    val applicant: Applicant,
    val interviewTime: LocalDateTime,
    val part: Part
)