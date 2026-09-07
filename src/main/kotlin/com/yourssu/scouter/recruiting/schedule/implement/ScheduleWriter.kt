package com.yourssu.scouter.recruiting.schedule.implement

import org.springframework.stereotype.Component

@Component
class ScheduleWriter(
    private val scheduleRepository: ScheduleRepository,
) {
    fun writeAll(schedules: List<Schedule>) {
        scheduleRepository.saveAll(schedules)
    }

    fun deleteAllByPart(partId: Long): Int {
        return scheduleRepository.deleteAllByPartId(partId)
    }

    fun deleteAll(ids: List<Long>) = scheduleRepository.deleteAllInBatch(ids)

    fun updateLocationById(
        scheduleId: Long,
        locationType: ScheduleLocationType,
        locationDetail: String?,
    ) {
        scheduleRepository.updateLocationById(scheduleId, locationType, locationDetail)
    }
}
