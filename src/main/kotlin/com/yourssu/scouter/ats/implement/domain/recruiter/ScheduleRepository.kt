package com.yourssu.scouter.ats.implement.domain.recruiter

interface ScheduleRepository {
    fun saveAll(schedules: List<Schedule>)

    fun findAll(): List<ReadScheduleDto>

    fun findAllByPartId(partId: Long): List<ReadScheduleDto>

    fun deleteAllByPartId(partId: Long): Int

    fun deleteAllInBatch(ids: List<Long>)

    fun existsById(id: Long): Boolean

    fun updateLocationById(
        scheduleId: Long,
        locationType: ScheduleLocationType,
        locationDetail: String?,
    )
}
