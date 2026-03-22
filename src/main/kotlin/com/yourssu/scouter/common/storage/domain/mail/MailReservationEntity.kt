package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.MailReservationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "mail_reservation")
class MailReservationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    val mailId: Long,

    @Column(nullable = false)
    val reservationTime: Instant,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: MailReservationStatus = MailReservationStatus.SCHEDULED,

    @Column
    val claimedAt: Instant? = null,
) {

    companion object {
        fun from(mailReservation: MailReservation): MailReservationEntity {
            return MailReservationEntity(
                id = mailReservation.id,
                mailId = mailReservation.mailId,
                reservationTime = mailReservation.reservationTime,
                status = mailReservation.status,
                claimedAt = mailReservation.claimedAt,
            )
        }
    }

    fun toDomain(): MailReservation {
        return MailReservation(
            id = id,
            mailId = mailId,
            reservationTime = reservationTime,
            status = status,
            claimedAt = claimedAt,
        )
    }
}
