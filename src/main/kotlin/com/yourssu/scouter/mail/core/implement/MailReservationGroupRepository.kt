package com.yourssu.scouter.mail.core.implement

interface MailReservationGroupRepository {

    fun save(group: MailReservationGroup): MailReservationGroup

    fun findAll(): List<MailReservationGroup>

    fun findById(id: Long): MailReservationGroup?

    fun findAllByReservedByUserIds(reservedByUserIds: Collection<Long>): List<MailReservationGroup>

    fun findAllByStatus(status: MailReservationStatus): List<MailReservationGroup>

    fun updateStatus(
        id: Long,
        status: MailReservationStatus,
    )

    fun deleteById(id: Long)
}
