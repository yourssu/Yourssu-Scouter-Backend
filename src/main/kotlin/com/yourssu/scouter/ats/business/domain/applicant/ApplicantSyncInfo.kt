package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.common.implement.support.google.ResponseItem

data class ApplicantSyncInfo(
    val applicant: Applicant,
    val formId: String,
    val responseId: String,
    val unmappedResponseItems: List<ResponseItem> = emptyList(),
)
