package com.yourssu.scouter.common.business.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationStatus
import java.time.Instant

data class MailGroupDetail(
    val groupId: Long,
    val reserverName: String?,
    val reserverEmail: String?,
    val templateId: Long?,
    val reservationTime: Instant,
    val status: MailReservationStatus,
    val createdAt: Instant,
    val mails: List<MailSummary>,
) {
    data class MailSummary(
        val reservationId: Long,
        val receiverEmail: String,
        val mailSubject: String,
    )
}
