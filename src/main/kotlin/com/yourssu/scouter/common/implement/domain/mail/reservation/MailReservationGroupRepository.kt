package com.yourssu.scouter.common.implement.domain.mail.reservation

interface MailReservationGroupRepository {

    fun save(group: MailReservationGroup): MailReservationGroup

    fun findAll(): List<MailReservationGroup>

    fun findById(id: Long): MailReservationGroup?

    fun findAllByReservedByUserIds(reservedByUserIds: Collection<Long>): List<MailReservationGroup>

    fun deleteById(id: Long)
}
