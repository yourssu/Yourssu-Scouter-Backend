package com.yourssu.scouter.common.business.domain.mail

import java.time.Instant

data class MailReservationDetail(
    val reservationId: Long,
    val mailId: Long,
    val reservationTime: Instant,
    val senderEmailAddress: String,
    val receiverEmailAddresses: List<String>,
    val ccEmailAddresses: List<String>,
    val bccEmailAddresses: List<String>,
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val hasAttachments: Boolean,
)

