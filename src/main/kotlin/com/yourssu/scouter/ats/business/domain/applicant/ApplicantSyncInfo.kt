package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant

data class ApplicantSyncInfo(
    val applicant: Applicant,
    val formId: String,
    val responseId: String,
)
