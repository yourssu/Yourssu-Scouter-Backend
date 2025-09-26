package com.yourssu.scouter.ats.storage.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleRepository
import com.yourssu.scouter.ats.implement.support.exception.DuplicateScheduleException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Repository

@Repository
class ScheduleRepositoryImpl(
    private val jpaScheduleRepository: JpaScheduleRepository,
) : ScheduleRepository {

    override fun saveAll(schedules: List<Schedule>) {
        val entities = ScheduleEntity.fromDomainList(schedules)
        try {
            jpaScheduleRepository.saveAll(entities)
        } catch (e: DataIntegrityViolationException) {
            if (e.message?.contains("unique_interview_schedule") == true)
                throw DuplicateScheduleException("이미 해당 시간에 면접이 예정되어 있습니다.")
            throw e
        }
    }

    override fun findAllByPartId(partId: Long) : List<Schedule> {
        val entities = jpaScheduleRepository.findAllByPartId(partId)
        return ScheduleEntity.toDomainList(entities)
    }

}