package com.yourssu.scouter.common.implement.domain.mail.reservation

interface MailReservationGroupRepository {

    fun save(group: MailReservationGroup): MailReservationGroup

    fun findAll(): List<MailReservationGroup>

    fun findById(id: Long): MailReservationGroup?

    fun findAllBySenderEmail(senderEmail: String): List<MailReservationGroup>

    fun findAllBySenderEmails(senderEmails: List<String>): List<MailReservationGroup>

    fun deleteById(id: Long)
}
