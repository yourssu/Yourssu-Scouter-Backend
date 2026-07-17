package com.yourssu.scouter.ats.implement.domain.applicant

class ApplicantAnswer(
    val id: Long? = null,
    val applicantId: Long,
    val sectionId: Long? = null,
    val question: String,
    val answer: String,
)
