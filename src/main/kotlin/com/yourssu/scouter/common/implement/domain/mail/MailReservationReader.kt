package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MailReservationReader(
    private val mailReservationRepository: MailReservationRepository,
) {

    fun readAllBefore(time: Instant): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqual(time)
    }

    fun readAllBeforeBySenderEmail(time: Instant, senderEmail: String): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqualAndSenderEmail(time, senderEmail)
    }
}
