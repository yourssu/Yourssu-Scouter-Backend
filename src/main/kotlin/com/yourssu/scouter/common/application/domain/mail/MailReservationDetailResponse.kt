package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailReservationDetail
import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class MailReservationDetailResponse(
    val reservationId: Long,
    val mailId: Long,
    val reservationTime: Instant,
    @Schema(description = "예약 상태", example = "SCHEDULED", allowableValues = ["SCHEDULED", "PENDING_SEND", "SENT"])
    val status: MailReservationStatus,
    @Schema(description = "발신자 이메일", example = "sender@example.com")
    val senderEmailAddress: String,
    val mailSubject: String,
    val mailBody: String,
    val bodyFormat: String,
    val receiverEmailAddresses: List<String>,
    val ccEmailAddresses: List<String>,
    val bccEmailAddresses: List<String>,
    @field:Schema(description = "첨부파일 참조 목록")
    val attachmentReferences: List<AttachmentReference>,
) {
    @Schema(description = "첨부파일 참조 정보")
    data class AttachmentReference(
        @field:Schema(description = "업로드된 파일 ID", example = "11", nullable = true)
        val fileId: Long?,
        @field:Schema(description = "파일명", example = "guide.pdf")
        val fileName: String,
        @field:Schema(description = "파일 MIME 타입", example = "application/pdf")
        val contentType: String,
        @field:Schema(description = "S3 저장 키", example = "mail-files/attachment/uuid-guide.pdf")
        val storageKey: String,
    ) {
        companion object {
            fun from(reference: MailAttachmentReference): AttachmentReference {
                return AttachmentReference(
                    fileId = reference.fileId,
                    fileName = reference.fileName,
                    contentType = reference.contentType,
                    storageKey = reference.storageKey,
                )
            }
        }
    }

    companion object {
        fun from(detail: MailReservationDetail): MailReservationDetailResponse {
            return MailReservationDetailResponse(
                reservationId = detail.reservationId,
                mailId = detail.mailId,
                reservationTime = detail.reservationTime,
                status = detail.status,
                senderEmailAddress = detail.senderEmailAddress,
                mailSubject = detail.mailSubject,
                mailBody = detail.mailBody,
                bodyFormat = detail.bodyFormat.name,
                receiverEmailAddresses = detail.receiverEmailAddresses,
                ccEmailAddresses = detail.ccEmailAddresses,
                bccEmailAddresses = detail.bccEmailAddresses,
                attachmentReferences = detail.attachmentReferences.map { AttachmentReference.from(it) },
            )
        }
    }
}
