package com.yourssu.scouter.ats.implement.domain.recruiter

interface ScheduleRepository {
    fun saveAll(schedules: List<Schedule>)
    fun findAllByPartId(partId: Long): List<Schedule>
}