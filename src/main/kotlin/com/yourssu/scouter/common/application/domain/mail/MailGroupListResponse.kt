package com.yourssu.scouter.common.application.domain.mail

import com.yourssu.scouter.common.business.domain.mail.MailGroupDetail
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationStatus
import java.time.Instant

data class MailGroupListResponse(
    val groups: List<MailGroupItem>,
) {
    data class MailGroupItem(
        val groupId: Long,
        val senderEmail: String,
        val templateId: Long?,
        val reservationTime: Instant,
        val status: MailReservationStatus,
        val createdAt: Instant,
        val mailIds: List<Long>,
    )

    companion object {
        fun from(details: List<MailGroupDetail>): MailGroupListResponse =
            MailGroupListResponse(
                groups = details.map { detail ->
                    MailGroupItem(
                        groupId = detail.groupId,
                        senderEmail = detail.senderEmail,
                        templateId = detail.templateId,
                        reservationTime = detail.reservationTime,
                        status = detail.status,
                        createdAt = detail.createdAt,
                        mailIds = detail.mailIds,
                    )
                },
            )
    }
}
