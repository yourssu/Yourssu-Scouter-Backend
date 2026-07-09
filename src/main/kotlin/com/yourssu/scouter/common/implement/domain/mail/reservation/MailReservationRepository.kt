package com.yourssu.scouter.common.implement.domain.mail.reservation

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

    fun markAsSent(id: Long)

    fun markAsPendingSend(id: Long)

    fun tryClaimForSending(
        id: Long,
        claimedAt: Instant,
        now: Instant,
    ): Int

    fun resetStuckSendingReservations(claimedBefore: Instant): Int

    fun findAllByReservationTimeLessThanEqualAndReservedByUserIds(
        time: Instant,
        reservedByUserIds: Collection<Long>,
    ): List<MailReservation>

    fun findAllByReservedByUserIds(reservedByUserIds: Collection<Long>): List<MailReservation>

    fun findById(id: Long): MailReservation?

    fun findAllByGroupId(groupId: Long): List<MailReservation>

    fun deleteById(id: Long)

    fun updateReservationTime(
        id: Long,
        reservationTime: Instant,
    )
}
