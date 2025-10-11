package com.yourssu.scouter.ats.implement.domain.recruiter

import org.springframework.stereotype.Component

@Component
class ScheduleWriter(
    private val scheduleRepository: ScheduleRepository,
) {
    fun writeAll(schedules: List<Schedule>) {
        scheduleRepository.saveAll(schedules)
    }
}