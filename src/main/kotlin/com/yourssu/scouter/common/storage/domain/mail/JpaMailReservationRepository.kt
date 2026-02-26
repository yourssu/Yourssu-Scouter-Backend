package com.yourssu.scouter.common.storage.domain.mail

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface JpaMailReservationRepository : JpaRepository<MailReservationEntity, Long> {
    fun findAllByReservationTimeLessThanEqual(reservationTime: Instant): List<MailReservationEntity>

    @Query(
        "SELECT r FROM MailReservationEntity r, MailEntity m WHERE r.mailId = m.id " +
            "AND r.reservationTime <= :time AND m.senderEmailAddress = :senderEmail",
    )
    fun findAllByReservationTimeLessThanEqualAndSenderEmail(
        @Param("time") time: Instant,
        @Param("senderEmail") senderEmail: String,
    ): List<MailReservationEntity>
}
