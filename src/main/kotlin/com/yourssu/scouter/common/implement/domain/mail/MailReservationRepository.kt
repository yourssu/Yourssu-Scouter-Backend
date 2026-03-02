package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant

interface MailReservationRepository {

    fun save(mailReservation: MailReservation): MailReservation

    fun findAllByReservationTimeLessThanEqual(reservationTime: Instant): List<MailReservation>

    fun findAllByReservationTimeLessThanEqualAndSenderEmail(time: Instant, senderEmail: String): List<MailReservation>

    fun findAllBySenderEmail(senderEmail: String): List<MailReservation>

    fun findAllBySenderEmailAndReservationTimeBetween(
        senderEmail: String,
        from: Instant,
        to: Instant,
    ): List<MailReservation>

    fun findById(id: Long): MailReservation?

    fun deleteById(id: Long)
}
