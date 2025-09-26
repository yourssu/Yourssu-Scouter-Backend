package com.yourssu.scouter.ats.implement.domain.recruiter

import org.springframework.stereotype.Component

@Component
class ScheduleReader(
    private val scheduleRepository: ScheduleRepository,
) {
    fun readAllByPartId(partId: Long) : List<Schedule> {
        return scheduleRepository.findAllByPartId(partId)
    }
}