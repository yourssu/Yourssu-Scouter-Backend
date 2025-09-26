package com.yourssu.scouter.ats.business.domain.recruiter

data class CreateScheduleCommand (
    val applicantId: Long,
    val interviewTime: String,
    val partId: Long,
)