package com.yourssu.scouter.ats.storage.domain.recruiter

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaScheduleRepository : JpaRepository<ScheduleEntity, Long> {

    fun findAllByPartId(partId: Long): List<ScheduleEntity>

    @Query(
        """
        SELECT new com.yourssu.scouter.ats.storage.domain.recruiter.ScheduleWithNames(
            s.id, a.id, a.name, p.name, s.startTime, s.endTime
        )
        FROM ScheduleEntity s
        JOIN s.part p
        JOIN s.applicant a
        WHERE s.part.id = :partId
    """
    )
    fun findAllWithNamesByPartId(partId: Long): List<ScheduleWithNames>

    @Modifying
    @Query("DELETE FROM ScheduleEntity s WHERE s.part.id = :partId")
    fun deleteAllByPartId(partId: Long): Int

    fun deleteAllByIdIn(ids: List<Long>)
}