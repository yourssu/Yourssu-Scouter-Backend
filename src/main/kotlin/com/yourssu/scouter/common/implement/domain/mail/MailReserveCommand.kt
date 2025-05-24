package com.yourssu.scouter.common.implement.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.hrms.implement.domain.member.Member
import jakarta.mail.util.ByteArrayDataSource
import java.time.LocalDateTime
import org.springframework.web.multipart.MultipartFile

data class MailReserveCommand(
    val senderMemberId: Long,
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
        sender: Member,
    ): Mail {
        return Mail(
            senderEmailAddress = sender.email,
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
}
