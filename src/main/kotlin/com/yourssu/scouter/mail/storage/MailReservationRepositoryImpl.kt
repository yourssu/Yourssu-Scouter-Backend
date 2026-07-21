package com.yourssu.scouter.mail.storage

import com.yourssu.scouter.mail.implement.reservation.MailReservation
import com.yourssu.scouter.mail.implement.reservation.MailReservationRepository
import com.yourssu.scouter.mail.implement.reservation.MailReservationStatus
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
@Transactional(readOnly = true)
class MailReservationRepositoryImpl(
    private val jpaMailRepository: JpaMailRepository,
    private val mailEntityMapper: MailEntityMapper,
) : MailReservationRepository {

    @Transactional
    override fun save(mailReservation: MailReservation): MailReservation {
        val entity = mailEntityMapper.toEntity(mailReservation)
        val saved = jpaMailRepository.save(entity)
        return mailReservation.copy(id = saved.id)
    }

    override fun findAll(): List<MailReservation> =
        jpaMailRepository.findAll().map { it.toDomain() }

    override fun findAllByReservationTimeLessThanEqual(reservationTime: Instant): List<MailReservation> =
        jpaMailRepository.findAllByReservationTimeLessThanEqual(reservationTime).map { it.toDomain() }

    override fun findAllByReservationTimeLessThanEqualAndStatusNot(
        reservationTime: Instant,
        status: MailReservationStatus,
    ): List<MailReservation> =
        jpaMailRepository.findAllByReservationTimeLessThanEqualAndStatusNot(reservationTime, status)
            .map { it.toDomain() }

    override fun findAllByReservationTimeLessThanEqualAndStatusIn(
        reservationTime: Instant,
        statuses: Collection<MailReservationStatus>,
    ): List<MailReservation> =
        jpaMailRepository.findAllByReservationTimeLessThanEqualAndStatusIn(reservationTime, statuses)
            .map { it.toDomain() }

    @Transactional
    override fun markAsSent(id: Long) {
        jpaMailRepository.markAsSentNative(id)
    }

    @Transactional
    override fun markAsPendingSend(id: Long) {
        jpaMailRepository.markAsPendingSendNative(id)
    }

    @Transactional
    override fun tryClaimForSending(
        id: Long,
        claimedAt: Instant,
        now: Instant,
    ): Int = jpaMailRepository.tryClaimForSendingNative(id, claimedAt, now)

    @Transactional
    override fun resetStuckSendingReservations(claimedBefore: Instant): Int =
        jpaMailRepository.resetStuckSendingReservationsNative(claimedBefore)

    override fun findAllByReservationTimeLessThanEqualAndReservedByUserIds(
        time: Instant,
        reservedByUserIds: Collection<Long>,
    ): List<MailReservation> =
        jpaMailRepository.findAllByReservationTimeLessThanEqualAndReservedByUserIdIn(time, reservedByUserIds)
            .map { it.toDomain() }

    override fun findAllByReservedByUserIds(reservedByUserIds: Collection<Long>): List<MailReservation> =
        jpaMailRepository.findAllByReservedByUserIdIn(reservedByUserIds).map { it.toDomain() }

    override fun findById(id: Long): MailReservation? =
        jpaMailRepository.findById(id).orElse(null)?.toDomain()

    override fun findAllByGroupId(groupId: Long): List<MailReservation> =
        jpaMailRepository.findAllByGroupId(groupId).map { it.toDomain() }

    @Transactional
    override fun deleteById(id: Long) {
        jpaMailRepository.deleteById(id)
    }

    @Transactional
    override fun updateReservationTime(
        id: Long,
        reservationTime: Instant,
    ) {
        jpaMailRepository.updateReservationTimeNative(id, reservationTime)
    }
}
