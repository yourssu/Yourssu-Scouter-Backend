package com.yourssu.scouter.common.storage.domain.mail

import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDateTime

interface JpaMailReservationRepository : JpaRepository<MailReservationEntity, Long> {
    fun findAllByReservationTimeLessThanEqual(reservationTime: LocalDateTime): List<MailReservationEntity>
}
