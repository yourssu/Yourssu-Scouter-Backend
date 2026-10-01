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

    /** 소속 메일이 없을 때만 그룹을 삭제한다. 삭제된 행 수를 반환한다. */
    fun deleteByIdIfEmpty(id: Long): Int
}
