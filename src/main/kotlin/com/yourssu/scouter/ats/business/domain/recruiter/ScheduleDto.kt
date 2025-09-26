package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantDto
import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.common.business.domain.part.PartDto
import java.time.LocalDateTime

data class ScheduleDto(
    val id: Long,
    val applicant: ApplicantDto,
    val interviewTime: LocalDateTime,
    val part: PartDto,
) {
    companion object {
        fun from(schedule: Schedule): ScheduleDto = ScheduleDto(
            id = schedule.id ?: throw IllegalStateException("Schedule ID는 Null일 수 없습니다"),
            applicant = ApplicantDto.from(schedule.applicant),
            interviewTime = schedule.interviewTime,
            part = PartDto.from(schedule.part),
        )

        fun fromDomainList(schedules: List<Schedule>): List<ScheduleDto> = schedules.map(::from)

    }

}