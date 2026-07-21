package com.yourssu.scouter.mail.business

import java.time.Instant

data class MailReserveCommand(
    val senderUserId: Long,
    val templateId: Long,
    val reservationTime: Instant,
    val recipients: List<RecipientCommand>,
    val sharedBindings: Map<String, String> = emptyMap(),
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
) {
    data class RecipientCommand(
        val applicantId: Long,
        val bindings: Map<String, String> = emptyMap(),
    )
}
