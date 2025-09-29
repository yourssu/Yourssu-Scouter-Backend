package com.yourssu.scouter.ats.implement.domain.recruiter

interface ScheduleRepository {
    fun saveAll(schedules: List<InterviewSchedule>)
    fun findAllByPartId(partId: Long): List<InterviewSchedule>
}