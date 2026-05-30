package com.yourssu.scouter.common.implement.domain.mail.reservation

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

    fun readAllBySenderEmail(senderEmail: String): List<MailReservationGroup> =
        mailReservationGroupRepository.findAllBySenderEmail(senderEmail)

    fun readAllBySenderEmails(senderEmails: List<String>): List<MailReservationGroup> =
        mailReservationGroupRepository.findAllBySenderEmails(senderEmails)
}
