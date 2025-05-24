package com.yourssu.scouter.common.implement.domain.mail

import java.time.LocalDateTime
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MailReservationReader(
    private val mailReservationRepository: MailReservationRepository,
) {

    fun readAllBefore(time: LocalDateTime): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqual(time)
    }
}
