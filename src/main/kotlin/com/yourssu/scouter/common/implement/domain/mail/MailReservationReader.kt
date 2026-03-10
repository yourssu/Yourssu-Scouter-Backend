package com.yourssu.scouter.common.implement.domain.mail

import java.time.Instant
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MailReservationReader(
    private val mailReservationRepository: MailReservationRepository,
) {

    fun readAll(): List<MailReservation> {
        return mailReservationRepository.findAll()
    }

    fun readAllBefore(time: Instant): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqual(time)
    }

    /** 발송 대기 중인 예약만 조회 (status != SENT) */
    fun readAllPendingBefore(time: Instant): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqualAndStatusNot(time, MailReservationStatus.SENT)
    }

    fun readAllBeforeBySenderEmail(time: Instant, senderEmail: String): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqualAndSenderEmail(time, senderEmail)
    }

    fun readAllBeforeBySenderEmails(time: Instant, senderEmails: List<String>): List<MailReservation> {
        return mailReservationRepository.findAllByReservationTimeLessThanEqualAndSenderEmails(time, senderEmails)
    }

    fun readAllBySenderEmail(senderEmail: String): List<MailReservation> {
        return mailReservationRepository.findAllBySenderEmail(senderEmail)
    }

    fun readAllBySenderEmails(senderEmails: List<String>): List<MailReservation> {
        return mailReservationRepository.findAllBySenderEmails(senderEmails)
    }

    fun readAllBySenderEmailAndReservationTimeBetween(
        senderEmail: String,
        from: Instant,
        to: Instant,
    ): List<MailReservation> {
        return mailReservationRepository.findAllBySenderEmailAndReservationTimeBetween(senderEmail, from, to)
    }

    fun readAllBySenderEmailsAndReservationTimeBetween(
        senderEmails: List<String>,
        from: Instant,
        to: Instant,
    ): List<MailReservation> {
        return mailReservationRepository.findAllBySenderEmailsAndReservationTimeBetween(senderEmails, from, to)
    }

    fun readById(id: Long): MailReservation? {
        return mailReservationRepository.findById(id)
    }
}
