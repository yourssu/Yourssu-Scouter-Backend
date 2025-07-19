package com.yourssu.scouter.common.application.domain.mail

import java.time.LocalDateTime
import org.springframework.format.annotation.DateTimeFormat

data class MailReserveRequest(
    val receiverEmailAddresses: List<String>,
    val ccEmailAddresses: List<String>?,
    val bccEmailAddresses: List<String>?,
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: String,
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val reservationTime: LocalDateTime
)
