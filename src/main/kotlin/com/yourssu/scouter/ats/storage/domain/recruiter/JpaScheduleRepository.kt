package com.yourssu.scouter.ats.storage.domain.recruiter

import org.springframework.data.jpa.repository.JpaRepository

interface JpaScheduleRepository : JpaRepository<ScheduleEntity, Long> {

    fun findByPartId(partId: Long): List<ScheduleEntity>
}