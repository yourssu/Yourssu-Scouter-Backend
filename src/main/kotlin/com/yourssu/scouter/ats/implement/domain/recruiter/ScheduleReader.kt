package com.yourssu.scouter.ats.implement.domain.recruiter

import org.springframework.stereotype.Component

@Component
class ScheduleReader(
    private val scheduleRepository: ScheduleRepository,
) {
    fun readAllByPartId(partId: Long) : List<InterviewSchedule> {
        return scheduleRepository.findAllByPartId(partId)
    }
}