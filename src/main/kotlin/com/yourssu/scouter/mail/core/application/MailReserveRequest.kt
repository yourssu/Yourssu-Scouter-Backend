package com.yourssu.scouter.mail.core.application

import com.yourssu.scouter.mail.core.business.MailReserveCommand
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "메일 예약 요청")
data class MailReserveRequest(
    @field:Schema(description = "사용할 템플릿 ID", example = "42")
    val templateId: Long,
    @field:Schema(description = "예약 발송 시간 (ISO 8601, UTC)", example = "2026-02-06T02:00:00Z")
    val reservationTime: Instant,
    @field:Schema(description = "수신자 목록")
    val recipients: List<RecipientRequest>,
    @field:Schema(description = "수신자 공통 바인딩 (perRecipient=false 변수)")
    val sharedBindings: Map<String, String> = emptyMap(),
    @field:Schema(description = "참조(CC) 이메일 주소 목록", nullable = true)
    val ccEmailAddresses: List<String>? = null,
    @field:Schema(description = "숨은참조(BCC) 이메일 주소 목록", nullable = true)
    val bccEmailAddresses: List<String>? = null,
) {
    @Schema(description = "수신자 정보")
    data class RecipientRequest(
        @field:Schema(description = "수신자 이메일", example = "alice@example.com")
        val email: String,
        @field:Schema(description = "지원자 ID (있으면 이메일보다 우선 사용)", nullable = true)
        val applicantId: Long? = null,
        @field:Schema(description = "수신자별 바인딩 (perRecipient=true 변수)")
        val bindings: Map<String, String> = emptyMap(),
    )

    fun toCommand(userId: Long): MailReserveCommand {
        return MailReserveCommand(
            senderUserId = userId,
            templateId = templateId,
            reservationTime = reservationTime,
            recipients =
                recipients.map {
                    MailReserveCommand.RecipientCommand(
                        applicantId = it.applicantId ?: throw IllegalArgumentException("applicantId is required"),
                        bindings = it.bindings,
                    )
                },
            sharedBindings = sharedBindings,
            ccEmailAddresses = ccEmailAddresses ?: emptyList(),
            bccEmailAddresses = bccEmailAddresses ?: emptyList(),
        )
    }
}
