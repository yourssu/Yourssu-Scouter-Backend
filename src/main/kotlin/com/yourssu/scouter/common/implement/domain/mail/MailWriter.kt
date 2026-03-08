package com.yourssu.scouter.common.implement.domain.mail

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

    fun reserve(mail: Mail, reservationTime: Instant) {
        val savedMail: Mail = mailRepository.save(mail)
        val mailReservation = MailReservation(
            mailId = savedMail.id!!,
            reservationTime = reservationTime,
            status = MailReservationStatus.SCHEDULED,
        )

        mailReservationRepository.save(mailReservation)
        log.info(
            "메일 예약 저장 완료: mailId={}, reservationTime={}, subject=[{}]",
            savedMail.id,
            reservationTime,
            savedMail.mailSubject,
        )
    }
}
