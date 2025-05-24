package com.yourssu.scouter.common.implement.domain.mail

interface MailReservationRepository {

    fun save(mailReservation: MailReservation): MailReservation
}
