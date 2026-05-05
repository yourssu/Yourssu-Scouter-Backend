package com.yourssu.scouter.common.implement.domain.mail.message

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationRepository
import java.time.Instant
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MailWriter(
    private val mailRepository: MailRepository,
    private val mailReservationRepository: MailReservationRepository,
) {
    companion object {
        private val log = LoggerFactory.getLogger(MailWriter::class.java)
    }

    fun reserve(mail: Mail, reservationTime: Instant, groupId: Long) {
        mail.schedule(groupId, reservationTime)
        val savedMail = mailRepository.save(mail)

        log.info(
            "메일 예약 저장 완료: mailId={}, reservationTime={}, subject=[{}]",
            savedMail.id,
            reservationTime,
            savedMail.mailSubject,
        )
    }
}
