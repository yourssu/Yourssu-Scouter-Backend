package com.yourssu.scouter.ats.business.domain.recruiter

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantDto
import com.yourssu.scouter.ats.implement.domain.recruiter.InterviewSchedule
import com.yourssu.scouter.common.business.domain.part.PartDto
import java.time.LocalDateTime

data class ScheduleDto(
    val id: Long,
    val applicant: ApplicantDto,
    val interviewTime: LocalDateTime,
    val part: PartDto,
) {
    companion object {
        fun from(schedule: InterviewSchedule): ScheduleDto = ScheduleDto(
            id = schedule.id ?: throw IllegalStateException("Schedule ID should not be null"),
            applicant = ApplicantDto.from(schedule.applicant),
            interviewTime = schedule.interviewTime,
            part = PartDto.from(schedule.part),
        )

        fun fromDomainList(schedules: List<InterviewSchedule>): List<ScheduleDto> = schedules.map(::from)

    }

}