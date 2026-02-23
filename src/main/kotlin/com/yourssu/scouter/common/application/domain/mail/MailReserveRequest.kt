package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailBodyFormat
import com.yourssu.scouter.common.business.domain.mail.MailReserveCommand
import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "메일 예약 요청")
data class MailReserveRequest(
    @field:Schema(description = "수신자 이메일 주소 목록", example = "[\"user@example.com\"]")
    val receiverEmailAddresses: List<String>,
    @field:Schema(description = "참조(CC) 이메일 주소 목록", example = "[]", nullable = true)
    val ccEmailAddresses: List<String>?,
    @field:Schema(description = "숨은참조(BCC) 이메일 주소 목록", example = "[]", nullable = true)
    val bccEmailAddresses: List<String>?,
    @field:Schema(description = "메일 제목", example = "면접 안내")
    val mailSubject: String,
    @field:Schema(description = "메일 본문", example = "<p>안녕하세요</p>")
    val mailBody: String,
    @field:Schema(description = "본문 형식", example = "HTML", allowableValues = ["HTML", "PLAIN_TEXT"])
    val bodyFormat: String,
    @field:Schema(description = "예약 발송 시간 (ISO 8601, UTC)", example = "2026-02-06T02:00:00Z")
    val reservationTime: Instant,
    @field:Schema(description = "첨부파일 참조 목록")
    val attachmentReferences: List<AttachmentReferenceRequest> = emptyList(),
) {
    data class AttachmentReferenceRequest(
        @field:Schema(description = "업로드된 파일 ID", example = "11")
        val fileId: Long,
    )

    fun toCommand(userId: Long): MailReserveCommand {
        return MailReserveCommand(
            senderUserId = userId,
            receiverEmailAddresses = receiverEmailAddresses,
            ccEmailAddresses = ccEmailAddresses ?: emptyList(),
            bccEmailAddresses = bccEmailAddresses ?: emptyList(),
            mailSubject = mailSubject,
            mailBody = mailBody,
            bodyFormat =
                MailBodyFormat.entries.find { it.name.equals(bodyFormat, ignoreCase = true) }
                    ?: throw IllegalArgumentException("지원하지 않는 bodyFormat입니다: $bodyFormat (가능한 값: ${MailBodyFormat.entries.joinToString()})"),
            attachmentReferences =
                attachmentReferences.map {
                    MailAttachmentReference(
                        fileId = it.fileId,
                        fileName = "",
                        contentType = "",
                        storageKey = "",
                    )
                },
            reservationTime = reservationTime,
        )
    }
}
