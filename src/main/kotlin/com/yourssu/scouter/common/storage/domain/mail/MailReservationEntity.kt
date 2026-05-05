package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationStatus
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "mail_reservation")
class MailReservationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @OneToOne(cascade = [(CascadeType.ALL)], fetch = FetchType.LAZY)
    @JoinColumn(name = "mail_id", referencedColumnName = "id")
    var mail: MailEntity? = null,
    @Column(nullable = true)
    val groupId: Long? = null,
    @Column(nullable = false)
    val reservationTime: Instant,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: MailReservationStatus = MailReservationStatus.SCHEDULED,
    @Column
    val claimedAt: Instant? = null,
) {
    companion object {
        fun from(domain: MailReservation?): MailReservationEntity? {
            return domain?.let {
                MailReservationEntity(
                    id = domain.id,
                    groupId = domain.groupId,
                    reservationTime = domain.reservationTime,
                    status = domain.status,
                    claimedAt = domain.claimedAt,
                )
            }
        }
    }

    fun toDomain(): MailReservation {
        return MailReservation(
            id = id,
            mailId = requireNotNull(mail?.id) { "MailEntity must have an id" },
            groupId = groupId,
            reservationTime = reservationTime,
            status = status,
            claimedAt = claimedAt,
        )
    }
}
