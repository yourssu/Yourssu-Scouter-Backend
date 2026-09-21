package com.yourssu.scouter.mail.core.storage

import com.yourssu.scouter.mail.core.implement.MailReservationStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JpaMailReservationGroupRepository : JpaRepository<MailReservationGroupEntity, Long> {

    fun findAllByReservedByUserIdIn(reservedByUserIds: Collection<Long>): List<MailReservationGroupEntity>

    fun findAllByStatus(status: MailReservationStatus): List<MailReservationGroupEntity>

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MailReservationGroupEntity g SET g.status = :status WHERE g.id = :id")
    fun updateStatus(
        @Param("id") id: Long,
        @Param("status") status: MailReservationStatus,
    ): Int

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        value =
            "DELETE FROM mail_reservation_group WHERE id = :id " +
                "AND NOT EXISTS (SELECT 1 FROM mail WHERE group_id = :id)",
        nativeQuery = true,
    )
    fun deleteByIdIfEmpty(@Param("id") id: Long): Int
}
