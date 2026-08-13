package com.yourssu.scouter.mail.core.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMailReservationGroupRepository : JpaRepository<MailReservationGroupEntity, Long> {

    fun findAllByReservedByUserIdIn(reservedByUserIds: Collection<Long>): List<MailReservationGroupEntity>
}
