package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface JpaMailReservationRepository : JpaRepository<MailReservationEntity, Long> {

    fun findAllByReservationTimeLessThanEqual(reservationTime: Instant): List<MailReservationEntity>

    fun findAllByReservationTimeLessThanEqualAndStatusNot(
        reservationTime: Instant,
        status: MailReservationStatus,
    ): List<MailReservationEntity>

    @Query(
        "SELECT r FROM MailReservationEntity r, MailEntity m WHERE r.mailId = m.id " +
            "AND r.reservationTime <= :time AND m.senderEmailAddress = :senderEmail",
    )
    fun findAllByReservationTimeLessThanEqualAndSenderEmail(
        @Param("time") time: Instant,
        @Param("senderEmail") senderEmail: String,
    ): List<MailReservationEntity>

    @Query(
        "SELECT r FROM MailReservationEntity r, MailEntity m WHERE r.mailId = m.id " +
            "AND r.reservationTime <= :time AND m.senderEmailAddress IN :senderEmails",
    )
    fun findAllByReservationTimeLessThanEqualAndSenderEmails(
        @Param("time") time: Instant,
        @Param("senderEmails") senderEmails: List<String>,
    ): List<MailReservationEntity>

    @Query(
        "SELECT r FROM MailReservationEntity r, MailEntity m WHERE r.mailId = m.id " +
            "AND m.senderEmailAddress = :senderEmail",
    )
    fun findAllBySenderEmail(
        @Param("senderEmail") senderEmail: String,
    ): List<MailReservationEntity>

    @Query(
        "SELECT r FROM MailReservationEntity r, MailEntity m WHERE r.mailId = m.id " +
            "AND m.senderEmailAddress IN :senderEmails",
    )
    fun findAllBySenderEmails(
        @Param("senderEmails") senderEmails: List<String>,
    ): List<MailReservationEntity>

    @Query(
        "SELECT r FROM MailReservationEntity r, MailEntity m WHERE r.mailId = m.id " +
            "AND m.senderEmailAddress = :senderEmail " +
            "AND r.reservationTime BETWEEN :from AND :to",
    )
    fun findAllBySenderEmailAndReservationTimeBetween(
        @Param("senderEmail") senderEmail: String,
        @Param("from") from: Instant,
        @Param("to") to: Instant,
    ): List<MailReservationEntity>

    @Query(
        "SELECT r FROM MailReservationEntity r, MailEntity m WHERE r.mailId = m.id " +
            "AND m.senderEmailAddress IN :senderEmails " +
            "AND r.reservationTime BETWEEN :from AND :to",
    )
    fun findAllBySenderEmailsAndReservationTimeBetween(
        @Param("senderEmails") senderEmails: List<String>,
        @Param("from") from: Instant,
        @Param("to") to: Instant,
    ): List<MailReservationEntity>
}
