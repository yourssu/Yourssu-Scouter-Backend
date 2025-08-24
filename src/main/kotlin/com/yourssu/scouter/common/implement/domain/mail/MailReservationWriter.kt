package com.yourssu.scouter.common.implement.domain.mail

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MailReservationWriter(
    private val mailReservationRepository: MailReservationRepository,
) {

    fun delete(mailReservation: MailReservation) {
        mailReservationRepository.deleteById(mailReservation.id!!)
    }
}
