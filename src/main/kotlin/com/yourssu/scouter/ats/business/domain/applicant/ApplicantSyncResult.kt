package com.yourssu.scouter.ats.business.domain.applicant

data class ApplicantSyncResult(
    val successMessages: List<String>,
    val failureMessages: List<String>,
)
