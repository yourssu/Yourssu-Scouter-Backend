package com.yourssu.scouter.ats.business.domain.applicant

data class ApplicantSyncResult(
    val successeMessages: List<String>,
    val failureMessages: List<String>,
)
