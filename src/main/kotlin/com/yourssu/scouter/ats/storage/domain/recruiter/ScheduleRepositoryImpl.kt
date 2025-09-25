package com.yourssu.scouter.ats.storage.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.InterviewSchedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleRepository
import org.springframework.stereotype.Repository

@Repository
class ScheduleRepositoryImpl(
    private val jpaScheduleRepository: JpaScheduleRepository,
) : ScheduleRepository {


    override fun saveAll(schedules: List<InterviewSchedule>) {
        val entities = ScheduleEntity.fromAll(schedules)
        jpaScheduleRepository.saveAll(entities)

    }

    override fun findAllByPartId(partId: Long) : List<InterviewSchedule> {
        val entities = jpaScheduleRepository.findByPartId(partId)
        return ScheduleEntity.toDomains(entities)
    }


}