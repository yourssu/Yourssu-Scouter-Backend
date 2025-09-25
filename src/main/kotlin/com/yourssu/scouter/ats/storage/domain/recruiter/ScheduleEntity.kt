package com.yourssu.scouter.ats.storage.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.InterviewSchedule
import com.yourssu.scouter.ats.storage.domain.applicant.ApplicantEntity
import com.yourssu.scouter.common.storage.domain.part.PartEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "interview_schedule",
    uniqueConstraints = [UniqueConstraint(
        name = "unique_interview_schedule",
        columnNames = ["part_id", "interview_time"]
    )]
)
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

        fun fromDomainList(schedules: List<InterviewSchedule>) = schedules.map(::from)

        fun toDomain(schedule: ScheduleEntity) = InterviewSchedule(
            id = schedule.id,
            part = schedule.part.toDomain(),
            applicant = schedule.applicant.toDomain(),
            interviewTime = schedule.interviewTime,
        )

        fun toDomainList(schedules: List<ScheduleEntity>) = schedules.map(::toDomain)
    }
}