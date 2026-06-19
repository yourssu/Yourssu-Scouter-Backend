package com.yourssu.scouter.common.implement.domain.mail.message

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationRepository
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MailWriter(
    private val mailReservationRepository: MailReservationRepository,
) {
    companion object {
        private val log = LoggerFactory.getLogger(MailWriter::class.java)
    }

    fun reserve(
        mail: Mail,
        reservationTime: Instant,
        groupId: Long,
    ) {
        val reservation =
            MailReservation(
                senderEmailAddress = mail.senderEmailAddress,
                receiverEmailAddress = mail.receiverEmailAddress,
                ccEmailAddresses = mail.ccEmailAddresses,
                bccEmailAddresses = mail.bccEmailAddresses,
                mailSubject = mail.mailSubject,
                mailBody = mail.mailBody,
                bodyFormat = mail.bodyFormat,
                attachmentReferences = mail.attachmentReferences,
                groupId = groupId,
                reservationTime = reservationTime,
            )
        val saved = mailReservationRepository.save(reservation)
        log.info(
            "메일 예약 저장 완료: reservationId={}, reservationTime={}, subject=[{}]",
            saved.id,
            reservationTime,
            saved.mailSubject,
        )
    }
}
