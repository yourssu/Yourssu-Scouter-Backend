package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant

interface MailReservationRepository {

    fun save(mailReservation: MailReservation): MailReservation

    fun findAll(): List<MailReservation>

    fun findAllByReservationTimeLessThanEqual(reservationTime: Instant): List<MailReservation>

    fun findAllByReservationTimeLessThanEqualAndStatusNot(
        reservationTime: Instant,
        status: MailReservationStatus,
    ): List<MailReservation>

    fun findAllByReservationTimeLessThanEqualAndSenderEmail(time: Instant, senderEmail: String): List<MailReservation>

    fun findAllByReservationTimeLessThanEqualAndSenderEmails(time: Instant, senderEmails: List<String>): List<MailReservation>

    fun findAllBySenderEmail(senderEmail: String): List<MailReservation>

    fun findAllBySenderEmails(senderEmails: List<String>): List<MailReservation>

    fun findAllBySenderEmailAndReservationTimeBetween(
        senderEmail: String,
        from: Instant,
        to: Instant,
    ): List<MailReservation>

    fun findAllBySenderEmailsAndReservationTimeBetween(
        senderEmails: List<String>,
        from: Instant,
        to: Instant,
    ): List<MailReservation>

    fun findById(id: Long): MailReservation?

    fun deleteById(id: Long)
}
