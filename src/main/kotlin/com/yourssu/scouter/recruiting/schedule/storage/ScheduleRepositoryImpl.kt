package com.yourssu.scouter.recruiting.schedule.storage

import com.yourssu.scouter.recruiting.schedule.implement.ReadScheduleDto
import com.yourssu.scouter.recruiting.schedule.implement.Schedule
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleLocationType
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleRepository
import org.springframework.stereotype.Repository

@Repository
class ScheduleRepositoryImpl(
    private val jpaScheduleRepository: JpaScheduleRepository,
) : ScheduleRepository {
    override fun saveAll(schedules: List<Schedule>) {
        val entities = ScheduleEntity.fromDomainList(schedules)
        jpaScheduleRepository.saveAll(entities)
    }

    override fun findAll(): List<ReadScheduleDto> {
        return jpaScheduleRepository.findAllWithNames().map(ScheduleWithNames::toDomain)
    }

    override fun findAllByPartId(partId: Long): List<ReadScheduleDto> {
        val entities = jpaScheduleRepository.findAllWithNamesByPartId(partId)
        return entities.map(ScheduleWithNames::toDomain)
    }

    override fun deleteAllByPartId(partId: Long): Int {
        return jpaScheduleRepository.deleteAllByPartId(partId)
    }

    override fun deleteAllInBatch(ids: List<Long>) {
        jpaScheduleRepository.deleteAllByIdIn(ids)
    }

    override fun existsById(id: Long): Boolean {
        return jpaScheduleRepository.existsById(id)
    }

    override fun updateLocationById(
        scheduleId: Long,
        locationType: ScheduleLocationType,
        locationDetail: String?,
    ) {
        jpaScheduleRepository.updateLocationById(scheduleId, locationType, locationDetail)
    }
}
