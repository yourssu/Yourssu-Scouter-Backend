package com.yourssu.scouter.mail.business

interface MailRecipientLookup {
    fun findByIds(applicantIds: List<Long>): Map<Long, MailRecipientProfile>
}

data class MailRecipientProfile(
    val email: String,
    val attributes: Map<String, String>,
)
