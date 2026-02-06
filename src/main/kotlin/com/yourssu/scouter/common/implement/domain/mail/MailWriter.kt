package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MailWriter(
    private val mailRepository: MailRepository,
    private val mailReservationRepository: MailReservationRepository,
) {

    fun reserve(mail: Mail, reservationTime: Instant) {
        val savedMail: Mail = mailRepository.save(mail)
        val mailReservation = MailReservation(
            mailId = savedMail.id!!,
            reservationTime = reservationTime
        )

        mailReservationRepository.save(mailReservation)
    }
}
