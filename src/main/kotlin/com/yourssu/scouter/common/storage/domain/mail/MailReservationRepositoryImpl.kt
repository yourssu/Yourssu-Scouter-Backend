package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.MailReservationRepository
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class MailReservationRepositoryImpl(
    private val jpaMailReservationRepository: JpaMailReservationRepository,
) : MailReservationRepository {

    override fun save(mailReservation: MailReservation): MailReservation {
        return jpaMailReservationRepository.save(MailReservationEntity.from(mailReservation)).toDomain()
    }

    override fun findAll(): List<MailReservation> {
        return jpaMailReservationRepository.findAll().map { it.toDomain() }
    }

    override fun findAllByReservationTimeLessThanEqual(reservationTime: Instant): List<MailReservation> {
        return jpaMailReservationRepository.findAllByReservationTimeLessThanEqual(reservationTime).map { it.toDomain() }
    }

    override fun findAllByReservationTimeLessThanEqualAndStatusNot(
        reservationTime: Instant,
        status: MailReservationStatus,
    ): List<MailReservation> {
        return jpaMailReservationRepository.findAllByReservationTimeLessThanEqualAndStatusNot(reservationTime, status)
            .map { it.toDomain() }
    }

    override fun findAllByReservationTimeLessThanEqualAndStatusIn(
        reservationTime: Instant,
        statuses: Collection<MailReservationStatus>,
    ): List<MailReservation> {
        return jpaMailReservationRepository.findAllByReservationTimeLessThanEqualAndStatusIn(reservationTime, statuses)
            .map { it.toDomain() }
    }

    override fun tryClaimForSending(id: Long, claimedAt: Instant, now: Instant): Int {
        return jpaMailReservationRepository.tryClaimForSendingNative(id, claimedAt, now)
    }

    override fun resetStuckSendingReservations(claimedBefore: Instant): Int {
        return jpaMailReservationRepository.resetStuckSendingReservationsNative(claimedBefore)
    }

    override fun findAllByReservationTimeLessThanEqualAndSenderEmail(
        time: Instant,
        senderEmail: String,
    ): List<MailReservation> {
        return jpaMailReservationRepository.findAllByReservationTimeLessThanEqualAndSenderEmail(time, senderEmail)
            .map { it.toDomain() }
    }

    override fun findAllByReservationTimeLessThanEqualAndSenderEmails(
        time: Instant,
        senderEmails: List<String>,
    ): List<MailReservation> {
        return jpaMailReservationRepository.findAllByReservationTimeLessThanEqualAndSenderEmails(time, senderEmails)
            .map { it.toDomain() }
    }

    override fun findAllBySenderEmail(senderEmail: String): List<MailReservation> {
        return jpaMailReservationRepository.findAllBySenderEmail(senderEmail)
            .map { it.toDomain() }
    }

    override fun findAllBySenderEmails(senderEmails: List<String>): List<MailReservation> {
        return jpaMailReservationRepository.findAllBySenderEmails(senderEmails)
            .map { it.toDomain() }
    }

    override fun findAllBySenderEmailAndReservationTimeBetween(
        senderEmail: String,
        from: Instant,
        to: Instant,
    ): List<MailReservation> {
        return jpaMailReservationRepository.findAllBySenderEmailAndReservationTimeBetween(senderEmail, from, to)
            .map { it.toDomain() }
    }

    override fun findAllBySenderEmailsAndReservationTimeBetween(
        senderEmails: List<String>,
        from: Instant,
        to: Instant,
    ): List<MailReservation> {
        return jpaMailReservationRepository.findAllBySenderEmailsAndReservationTimeBetween(senderEmails, from, to)
            .map { it.toDomain() }
    }

    override fun findById(id: Long): MailReservation? {
        return jpaMailReservationRepository.findById(id).orElse(null)?.toDomain()
    }

    override fun deleteById(id: Long) {
        jpaMailReservationRepository.deleteById(id)
    }
}
