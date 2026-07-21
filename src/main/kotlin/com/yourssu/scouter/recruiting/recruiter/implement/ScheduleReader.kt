package com.yourssu.scouter.recruiting.recruiter.implement

import org.springframework.stereotype.Component

@Component
class ScheduleReader(
    private val scheduleRepository: ScheduleRepository,
) {
    fun readAllByPartId(partId: Long) : List<ReadScheduleDto> {
        return scheduleRepository.findAllByPartId(partId)
    }

    fun readAll() : List<ReadScheduleDto> {
        return scheduleRepository.findAll()
    }
    fun existsById(id: Long) = scheduleRepository.existsById(id)
}