package com.yourssu.scouter.mail.storage

import com.yourssu.scouter.mail.implement.reservation.MailReservationGroup
import com.yourssu.scouter.mail.implement.reservation.MailReservationStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

// 1 예약 요청 = 1 Group, 수신자별 MailReservation 행은 이 group_id를 참조
@Entity
@Table(name = "mail_reservation_group")
class MailReservationGroupEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    // 예약자 users.id (nullable: 레거시 데이터 backfill)
    @Column
    val reservedByUserId: Long?,

    // 어떤 템플릿으로 발송했는지 추적용 (nullable: 레거시 데이터 backfill)
    @Column
    val templateId: Long?,

    @Column(nullable = false)
    val reservationTime: Instant,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: MailReservationStatus = MailReservationStatus.SCHEDULED,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),
) {

    companion object {
        fun from(group: MailReservationGroup): MailReservationGroupEntity =
            MailReservationGroupEntity(
                id = group.id,
                reservedByUserId = group.reservedByUserId,
                templateId = group.templateId,
                reservationTime = group.reservationTime,
                status = group.status,
                createdAt = group.createdAt,
            )
    }

    fun toDomain(): MailReservationGroup =
        MailReservationGroup(
            id = id,
            reservedByUserId = reservedByUserId,
            templateId = templateId,
            reservationTime = reservationTime,
            status = status,
            createdAt = createdAt,
        )
}
