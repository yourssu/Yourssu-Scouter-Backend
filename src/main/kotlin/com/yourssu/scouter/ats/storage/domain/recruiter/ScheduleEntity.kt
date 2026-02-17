package com.yourssu.scouter.ats.storage.domain.recruiter

import com.yourssu.scouter.ats.implement.domain.recruiter.Schedule
import com.yourssu.scouter.ats.implement.domain.recruiter.ScheduleLocationType
import com.yourssu.scouter.ats.storage.domain.applicant.ApplicantEntity
import com.yourssu.scouter.common.storage.domain.part.PartEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "interview_schedule",
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_interview_schedule",
            columnNames = ["part_id", "start_time"],
        ),
    ],
)
class ScheduleEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = false, foreignKey = ForeignKey(name = "fk_interview_schedule_part"))
    val part: PartEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "applicant_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_interview_schedule_applicant"),
    )
    val applicant: ApplicantEntity,
    @Column(nullable = false)
    val startTime: Instant,
    @Column(nullable = false)
    val endTime: Instant,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default 'CLUB_ROOM'")
    val locationType: ScheduleLocationType = ScheduleLocationType.CLUB_ROOM,
    @Column(nullable = true)
    val locationDetail: String? = null,
) {
    companion object {
        fun from(schedule: Schedule) =
            ScheduleEntity(
                part = PartEntity.from(schedule.part),
                applicant = ApplicantEntity.from(schedule.applicant),
                startTime = schedule.startTime,
                endTime = schedule.endTime,
                locationType = schedule.locationType,
                locationDetail = schedule.locationDetail,
            )

        fun fromDomainList(schedules: List<Schedule>) = schedules.map(::from)
    }
}
