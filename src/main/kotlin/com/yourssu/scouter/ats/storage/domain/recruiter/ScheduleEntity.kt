package com.yourssu.scouter.ats.storage.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.InterviewSchedule
import com.yourssu.scouter.ats.storage.domain.applicant.ApplicantEntity
import com.yourssu.scouter.common.storage.domain.part.PartEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "interview_schedule")
class ScheduleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "part_id", nullable = false, foreignKey = ForeignKey(name = "fk_interview_schedule_part"))
    val part: PartEntity,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
        name = "applicant_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_interview_schedule_applicant")
    )
    val applicant: ApplicantEntity,

    @Column(nullable = false)
    val interviewTime: LocalDateTime,
) {
    companion object {

        fun from(schedule: InterviewSchedule) = ScheduleEntity(
            part = PartEntity.from(schedule.part),
            applicant = ApplicantEntity.from(schedule.applicant),
            interviewTime = schedule.interviewTime,
        )

        fun fromAll(schedules: List<InterviewSchedule>) = schedules.map { from(it) }

        fun toDomain(schedule: ScheduleEntity) = InterviewSchedule(
            id = schedule.id,
            part = schedule.part.toDomain(),
            applicant = schedule.applicant.toDomain(),
            interviewTime = schedule.interviewTime,
        )

        fun toDomains(schedules: List<ScheduleEntity>) = schedules.map { toDomain(it) }

        fun toPartIdSet(schedules: List<ScheduleEntity>) = schedules.mapNotNull { it.part.id }.toSet()

    }
}