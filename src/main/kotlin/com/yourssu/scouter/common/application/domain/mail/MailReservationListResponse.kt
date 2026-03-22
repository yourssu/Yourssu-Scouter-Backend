package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailReservationDetail
import com.yourssu.scouter.common.implement.domain.mail.MailAttachmentReference
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class MailReservationListItem(
    val reservationId: Long,
    val mailId: Long,
    val reservationTime: Instant,
    @Schema(description = "예약 상태", example = "SCHEDULED", allowableValues = ["SCHEDULED", "PENDING_SEND", "SENT"])
    val status: MailReservationStatus,
    @Schema(description = "발신자 이메일", example = "sender@example.com")
    val senderEmailAddress: String,
    val mailSubject: String,
    @Schema(description = "대표 수신자 이메일 (수신자가 없으면 null)", example = "receiver@example.com", nullable = true)
    val primaryReceiverEmailAddress: String?,
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
}

data class MailReservationListResponse(
    val items: List<MailReservationListItem>,
) {
    companion object {
        fun from(details: List<MailReservationDetail>): MailReservationListResponse {
            return MailReservationListResponse(
                items =
                    details.map { detail ->
                        MailReservationListItem(
                            reservationId = detail.reservationId,
                            mailId = detail.mailId,
                            reservationTime = detail.reservationTime,
                            status = detail.status,
                            senderEmailAddress = detail.senderEmailAddress,
                            mailSubject = detail.mailSubject,
                            primaryReceiverEmailAddress = detail.receiverEmailAddresses.firstOrNull(),
                            attachmentReferences = detail.attachmentReferences.map { MailReservationListItem.AttachmentReference.from(it) },
                        )
                    },
            )
        }
    }
}
