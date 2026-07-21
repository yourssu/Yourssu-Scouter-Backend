package com.yourssu.scouter.mail.application

import com.yourssu.scouter.mail.business.MailGroupDetail
import com.yourssu.scouter.mail.implement.reservation.MailReservationStatus
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

data class MailGroupListResponse(
    val groups: List<MailGroupItem>,
) {
    data class MailGroupItem(
        val groupId: Long,
        @field:Schema(description = "예약자 이름 (영문 닉네임)", example = "emin", nullable = true)
        val reserverName: String?,
        @field:Schema(description = "예약자 이메일", example = "emin.urssu@gmail.com", nullable = true)
        val reserverEmail: String?,
        val templateId: Long?,
        val reservationTime: Instant,
        val status: MailReservationStatus,
        val createdAt: Instant,
        @field:Schema(description = "그룹에 속한 메일 목록 (수신자별 1건)")
        val mails: List<MailItem>,
    )

    data class MailItem(
        @field:Schema(description = "예약 ID")
        val reservationId: Long,
        @field:Schema(description = "수신자 이메일", example = "alice@example.com")
        val receiverEmail: String,
        @field:Schema(description = "변수 렌더링이 완료된 메일 제목", example = "[유어슈] 홍길동님 서류 결과 안내")
        val mailSubject: String,
    )

    companion object {
        fun from(details: List<MailGroupDetail>): MailGroupListResponse =
            MailGroupListResponse(
                groups = details.map { detail ->
                    MailGroupItem(
                        groupId = detail.groupId,
                        reserverName = detail.reserverName,
                        reserverEmail = detail.reserverEmail,
                        templateId = detail.templateId,
                        reservationTime = detail.reservationTime,
                        status = detail.status,
                        createdAt = detail.createdAt,
                        mails = detail.mails.map { mail ->
                            MailItem(
                                reservationId = mail.reservationId,
                                receiverEmail = mail.receiverEmail,
                                mailSubject = mail.mailSubject,
                            )
                        },
                    )
                },
            )
    }
}
