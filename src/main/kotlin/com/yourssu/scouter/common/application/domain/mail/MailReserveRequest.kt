package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.common.implement.domain.mail.MailReserveCommand
import java.time.LocalDateTime
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.multipart.MultipartFile

data class MailReserveRequest(

    val receiverEmailAddresses: List<String>,

    val ccEmailAddresses: List<String>?,

    val bccEmailAddresses: List<String>?,

    val mailSubject: String,

    val mailBody: String,

    val bodyFormat: String,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val reservationTime: LocalDateTime
) {

    fun toCommand(
        userId: Long,
        request: MailReserveRequest,
        inlineImages: List<MultipartFile>?,
        attachments: List<MultipartFile>?
    ): MailReserveCommand {
        return MailReserveCommand(
            senderUserId = userId,
            receiverEmailAddresses = receiverEmailAddresses,
            ccEmailAddresses = ccEmailAddresses ?: emptyList(),
            bccEmailAddresses = bccEmailAddresses ?: emptyList(),
            mailSubject = mailSubject,
            mailBody = mailBody,
            bodyFormat = MailBodyFormat.entries.find { it.name.equals(bodyFormat, ignoreCase = true) }
                ?: throw IllegalArgumentException("지원하지 않는 bodyFormat입니다: $bodyFormat (가능한 값: ${MailBodyFormat.entries.joinToString()})"),
            inlineImages = inlineImages ?: emptyList(),
            attachments = attachments ?: emptyList(),
            reservationTime = request.reservationTime
        )
    }
}
