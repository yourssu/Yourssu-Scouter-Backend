package com.yourssu.scouter.recruiting.applicant.business

import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.common.google.ResponseItem

data class ApplicantSyncInfo(
    val applicant: Applicant,
    val formId: String,
    val responseId: String,
    val unmappedResponseItems: List<ResponseItem> = emptyList(),
)
