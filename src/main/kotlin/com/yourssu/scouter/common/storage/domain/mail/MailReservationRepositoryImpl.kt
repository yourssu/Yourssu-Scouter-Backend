package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.MailReservation
import com.yourssu.scouter.common.implement.domain.mail.MailReservationRepository
import org.springframework.stereotype.Repository

@Repository
class MailReservationRepositoryImpl(
    private val jpaMailReservationRepository: JpaMailReservationRepository,
) : MailReservationRepository {

    override fun save(mailReservation: MailReservation): MailReservation {
        return jpaMailReservationRepository.save(MailReservationEntity.from(mailReservation)).toDomain()
    }

    override fun findAllByReservationTimeLessThanEqual(reservationTime: java.time.LocalDateTime): List<MailReservation> {
        return jpaMailReservationRepository.findAllByReservationTimeLessThanEqual(reservationTime).map { it.toDomain() }
    }

    override fun deleteById(id: Long) {
        jpaMailReservationRepository.deleteById(id)
    }
}
