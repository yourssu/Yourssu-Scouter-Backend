package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationRepository
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationStatus
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MailReservationRepositoryImpl(
    private val jpaMailRepository: JpaMailRepository,
    private val mailEntityMapper: MailEntityMapper,
) : MailReservationRepository {

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

    override fun markAsSent(id: Long) {
        jpaMailRepository.markAsSentNative(id)
    }

    override fun markAsPendingSend(id: Long) {
        jpaMailRepository.markAsPendingSendNative(id)
    }

    override fun tryClaimForSending(
        id: Long,
        claimedAt: Instant,
        now: Instant,
    ): Int = jpaMailRepository.tryClaimForSendingNative(id, claimedAt, now)

    override fun resetStuckSendingReservations(claimedBefore: Instant): Int =
        jpaMailRepository.resetStuckSendingReservationsNative(claimedBefore)

    override fun findAllByReservationTimeLessThanEqualAndSenderEmail(
        time: Instant,
        senderEmail: String,
    ): List<MailReservation> =
        jpaMailRepository.findAllByReservationTimeLessThanEqualAndSenderEmailAddress(time, senderEmail)
            .map { it.toDomain() }

    override fun findAllByReservationTimeLessThanEqualAndSenderEmails(
        time: Instant,
        senderEmails: List<String>,
    ): List<MailReservation> =
        jpaMailRepository.findAllByReservationTimeLessThanEqualAndSenderEmailAddressIn(time, senderEmails)
            .map { it.toDomain() }

    override fun findAllBySenderEmail(senderEmail: String): List<MailReservation> =
        jpaMailRepository.findAllBySenderEmailAddress(senderEmail).map { it.toDomain() }

    override fun findAllBySenderEmails(senderEmails: List<String>): List<MailReservation> =
        jpaMailRepository.findAllBySenderEmailAddressIn(senderEmails).map { it.toDomain() }

    override fun findAllBySenderEmailAndReservationTimeBetween(
        senderEmail: String,
        from: Instant,
        to: Instant,
    ): List<MailReservation> =
        jpaMailRepository.findAllBySenderEmailAddressAndReservationTimeBetween(senderEmail, from, to)
            .map { it.toDomain() }

    override fun findAllBySenderEmailsAndReservationTimeBetween(
        senderEmails: List<String>,
        from: Instant,
        to: Instant,
    ): List<MailReservation> =
        jpaMailRepository.findAllBySenderEmailAddressInAndReservationTimeBetween(senderEmails, from, to)
            .map { it.toDomain() }

    override fun findById(id: Long): MailReservation? =
        jpaMailRepository.findById(id).orElse(null)?.toDomain()

    override fun findAllByGroupId(groupId: Long): List<MailReservation> =
        jpaMailRepository.findAllByGroupId(groupId).map { it.toDomain() }

    override fun deleteById(id: Long) {
        jpaMailRepository.deleteById(id)
    }

    override fun updateReservationTime(
        id: Long,
        reservationTime: Instant,
    ) {
        jpaMailRepository.updateReservationTimeNative(id, reservationTime)
    }
}
