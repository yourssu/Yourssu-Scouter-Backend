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

    fun findAllByReservationTimeLessThanEqualAndStatusIn(
        reservationTime: Instant,
        statuses: Collection<MailReservationStatus>,
    ): List<MailReservation>

    /**
     * SCHEDULED 또는 PENDING_SEND 이고 [reservationTime]이 [now] 이하인 행만 SENDING으로 바꾼다.
     * @return 갱신된 행 수 (1이면 이 인스턴스가 발송 담당)
     */
    fun tryClaimForSending(id: Long, claimedAt: Instant, now: Instant): Int

    /** SENDING이 [claimedBefore] 이전에 claim된 행을 PENDING_SEND로 되돌린다 (고착 복구). */
    fun resetStuckSendingReservations(claimedBefore: Instant): Int

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
