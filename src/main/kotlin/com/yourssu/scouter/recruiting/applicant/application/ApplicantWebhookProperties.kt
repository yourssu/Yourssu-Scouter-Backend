package com.yourssu.scouter.recruiting.applicant.application

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "scouter.applicant-webhook")
data class ApplicantWebhookProperties(
    val secret: String = "",
)
