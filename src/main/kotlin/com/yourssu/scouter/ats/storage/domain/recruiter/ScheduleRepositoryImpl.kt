package com.yourssu.scouter.ats.storage.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.ReadScheduleDto
import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleRepository
import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import org.springframework.stereotype.Repository

@Repository
class ScheduleRepositoryImpl(
    private val jpaScheduleRepository: JpaScheduleRepository,
) : ScheduleRepository {

    override fun saveAll(schedules: List<Schedule>) {
        val entities = ScheduleEntity.fromDomainList(schedules)
        try {
            jpaScheduleRepository.saveAll(entities)
        } catch (e: Exception) {
            if (e.message?.contains("unique_interview_schedule", ignoreCase = true) == true)
                throw DuplicateScheduleException("이미 해당 시간에 면접이 예정되어 있습니다.")
            throw e
        }
    }

    override fun findAllByPartId(partId: Long) : List<ReadScheduleDto> {
        val entities = jpaScheduleRepository.findAllWithNamesByPartId(partId)
        return entities.map(ScheduleWithNames::toDomain)
    }

}