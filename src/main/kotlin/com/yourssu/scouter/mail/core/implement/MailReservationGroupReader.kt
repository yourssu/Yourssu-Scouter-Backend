package com.yourssu.scouter.mail.core.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class MailReservationGroupReader(
    private val mailReservationGroupRepository: MailReservationGroupRepository,
) {

    fun readAll(): List<MailReservationGroup> =
        mailReservationGroupRepository.findAll()

    fun readById(id: Long): MailReservationGroup? =
        mailReservationGroupRepository.findById(id)

    fun readAllByReservedByUserIds(reservedByUserIds: Collection<Long>): List<MailReservationGroup> =
        mailReservationGroupRepository.findAllByReservedByUserIds(reservedByUserIds)
}
