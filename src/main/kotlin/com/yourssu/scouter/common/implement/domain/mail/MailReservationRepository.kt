package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant

interface MailReservationRepository {

    fun save(mailReservation: MailReservation): MailReservation
    fun findAllByReservationTimeLessThanEqual(reservationTime: Instant): List<MailReservation>
    fun deleteById(id: Long)
}
