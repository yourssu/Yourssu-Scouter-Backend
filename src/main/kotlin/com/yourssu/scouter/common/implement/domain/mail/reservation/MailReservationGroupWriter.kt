package com.yourssu.scouter.common.implement.domain.mail.reservation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class MailReservationGroupWriter(
    private val mailReservationGroupRepository: MailReservationGroupRepository,
) {

    fun save(group: MailReservationGroup): MailReservationGroup =
        mailReservationGroupRepository.save(group)

    fun delete(id: Long) =
        mailReservationGroupRepository.deleteById(id)
}
