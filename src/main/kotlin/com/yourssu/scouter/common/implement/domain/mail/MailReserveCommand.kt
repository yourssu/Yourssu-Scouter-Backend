package com.yourssu.scouter.common.implement.domain.mail

import com.yourssu.scouter.common.application.domain.mail.MailReserveRequest
import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import jakarta.mail.util.ByteArrayDataSource
import java.time.LocalDateTime
import org.springframework.web.multipart.MultipartFile

data class MailReserveCommand(
    val senderUserId: Long,
    val receiverEmailAddresses: List<String>,
    val ccEmailAddresses: List<String> = emptyList(),
    val bccEmailAddresses: List<String> = emptyList(),
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: MailBodyFormat,
    val inlineImages: List<MultipartFile> = emptyList(),
    val attachments: List<MultipartFile> = emptyList(),
    val reservationTime: LocalDateTime,
) {
    fun toMail(
        senderEmailAddress: String,
    ): Mail {
        return Mail(
            senderEmailAddress = senderEmailAddress,
            receiverEmailAddresses = receiverEmailAddresses,
            ccEmailAddresses = ccEmailAddresses,
            bccEmailAddresses = bccEmailAddresses,
            mailSubject = mailSubject,
            mailBody = mailBody,
            bodyFormat = bodyFormat,
            inlineImages = inlineImages.associate {
                it.name to ByteArrayDataSource(it.inputStream, it.contentType)
            },
            attachments = attachments.associate {
                it.name to ByteArrayDataSource(it.inputStream, it.contentType)
            }
        )
    }

    companion object {
        fun from(
            userId: Long,
            request: MailReserveRequest,
            inlineImages: List<MultipartFile>?,
            attachments: List<MultipartFile>?
        ): MailReserveCommand {
            return MailReserveCommand(
                senderUserId = userId,
                receiverEmailAddresses = request.receiverEmailAddresses,
                ccEmailAddresses = request.ccEmailAddresses ?: emptyList(),
                bccEmailAddresses = request.bccEmailAddresses ?: emptyList(),
                mailSubject = request.mailSubject,
                mailBody = request.mailBody,
                bodyFormat = com.yourssu.scouter.common.business.domain.mail.MailBodyFormat.valueOf(request.bodyFormat),
                inlineImages = inlineImages ?: emptyList(),
                attachments = attachments ?: emptyList(),
                reservationTime = request.reservationTime
            )
        }
    }
}
