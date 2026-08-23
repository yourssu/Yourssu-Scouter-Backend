package com.yourssu.scouter.recruiting.applicant.business.dto

import java.time.Instant

data class ApplicantWebhookCommand(
    val formId: String,
    val responseId: String,
    val createTime: Instant,
    val respondentEmail: String?,
    val items: List<ApplicantWebhookItemCommand>,
)

data class ApplicantWebhookItemCommand(
    val question: String,
    val answer: String,
    val isDescriptive: Boolean,
)
