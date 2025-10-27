package com.yourssu.scouter.ats.implement.domain.recruiter

interface ScheduleRepository {
    fun saveAll(schedules: List<Schedule>)
    fun findAllByPartId(partId: Long): List<ReadScheduleDto>
    fun deleteAllByPartId(partId: Long): Int
    fun deleteById(id: Long)
    fun existsById(id: Long): Boolean
}