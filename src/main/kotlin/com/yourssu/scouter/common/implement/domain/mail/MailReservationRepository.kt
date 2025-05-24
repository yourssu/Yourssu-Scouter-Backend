package com.yourssu.scouter.common.implement.domain.mail

interface MailReservationRepository {

    fun save(mailReservation: MailReservation): MailReservation
    fun findAllByReservationTimeLessThanEqual(reservationTime: java.time.LocalDateTime): List<MailReservation>
    fun deleteById(id: Long)
}
