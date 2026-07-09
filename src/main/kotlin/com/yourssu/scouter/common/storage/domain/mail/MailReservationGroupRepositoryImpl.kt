package com.yourssu.scouter.common.storage.domain.mail

import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationGroup
import com.yourssu.scouter.common.implement.domain.mail.reservation.MailReservationGroupRepository
import org.springframework.stereotype.Repository

@Repository
class MailReservationGroupRepositoryImpl(
    private val jpaMailReservationGroupRepository: JpaMailReservationGroupRepository,
) : MailReservationGroupRepository {

    override fun save(group: MailReservationGroup): MailReservationGroup =
        jpaMailReservationGroupRepository.save(MailReservationGroupEntity.from(group)).toDomain()

    override fun findAll(): List<MailReservationGroup> =
        jpaMailReservationGroupRepository.findAll().map { it.toDomain() }

    override fun findById(id: Long): MailReservationGroup? =
        jpaMailReservationGroupRepository.findById(id).orElse(null)?.toDomain()

    override fun findAllByReservedByUserIds(reservedByUserIds: Collection<Long>): List<MailReservationGroup> =
        jpaMailReservationGroupRepository.findAllByReservedByUserIdIn(reservedByUserIds).map { it.toDomain() }

    override fun deleteById(id: Long) =
        jpaMailReservationGroupRepository.deleteById(id)
}
