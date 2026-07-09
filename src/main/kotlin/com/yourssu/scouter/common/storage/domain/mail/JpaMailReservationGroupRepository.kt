package com.yourssu.scouter.common.storage.domain.mail

import org.springframework.data.jpa.repository.JpaRepository

interface JpaMailReservationGroupRepository : JpaRepository<MailReservationGroupEntity, Long> {

    fun findAllByReservedByUserIdIn(reservedByUserIds: Collection<Long>): List<MailReservationGroupEntity>
}
