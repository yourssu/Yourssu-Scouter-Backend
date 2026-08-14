package com.yourssu.scouter.mail.core.business

interface MailRecipientLookup {
    fun findByIds(applicantIds: List<Long>): Map<Long, MailRecipientProfile>
}

data class MailRecipientProfile(
    val email: String,
    val attributes: Map<String, String>,
)
