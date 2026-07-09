package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.Optional

interface JpaMailRepository : JpaRepository<MailEntity, Long> {
    override fun findById(id: Long): Optional<MailEntity>

    override fun findAll(): List<MailEntity>

    fun findAllByGroupId(groupId: Long): List<MailEntity>

    fun findAllByReservationTimeLessThanEqual(reservationTime: Instant): List<MailEntity>

    fun findAllByReservationTimeLessThanEqualAndStatusNot(
        reservationTime: Instant,
        status: MailReservationStatus,
    ): List<MailEntity>

    fun findAllByReservationTimeLessThanEqualAndStatusIn(
        reservationTime: Instant,
        statuses: Collection<MailReservationStatus>,
    ): List<MailEntity>

    fun findAllByReservedByUserIdIn(reservedByUserIds: Collection<Long>): List<MailEntity>

    fun findAllByReservationTimeLessThanEqualAndReservedByUserIdIn(
        reservationTime: Instant,
        reservedByUserIds: Collection<Long>,
    ): List<MailEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value =
            "UPDATE mail SET status = 'SENDING', claimed_at = :claimedAt " +
                "WHERE id = :id AND status IN ('SCHEDULED', 'PENDING_SEND') AND reservation_time <= :now",
        nativeQuery = true,
    )
    fun tryClaimForSendingNative(
        @Param("id") id: Long,
        @Param("claimedAt") claimedAt: Instant,
        @Param("now") now: Instant,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = "UPDATE mail SET status = 'SENT', claimed_at = NULL WHERE id = :id",
        nativeQuery = true,
    )
    fun markAsSentNative(@Param("id") id: Long): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = "UPDATE mail SET status = 'PENDING_SEND', claimed_at = NULL WHERE id = :id",
        nativeQuery = true,
    )
    fun markAsPendingSendNative(@Param("id") id: Long): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value =
            "UPDATE mail SET status = 'PENDING_SEND', claimed_at = NULL " +
                "WHERE status = 'SENDING' AND claimed_at IS NOT NULL AND claimed_at < :threshold",
        nativeQuery = true,
    )
    fun resetStuckSendingReservationsNative(
        @Param("threshold") threshold: Instant,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value = "UPDATE mail SET reservation_time = :reservationTime WHERE id = :id",
        nativeQuery = true,
    )
    fun updateReservationTimeNative(
        @Param("id") id: Long,
        @Param("reservationTime") reservationTime: Instant,
    )
}
