package com.yourssu.scouter.recruiting.schedule.storage

import com.yourssu.scouter.recruiting.schedule.implement.Schedule
import com.yourssu.scouter.recruiting.schedule.implement.ScheduleLocationType
import com.yourssu.scouter.recruiting.applicant.storage.ApplicantEntity
import com.yourssu.scouter.masterdata.part.storage.PartEntity
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(
    name = "interview_schedule",
    // FK fk_interview_schedule_part 를 받쳐주는 인덱스. (part_id, start_time) 유니크 인덱스를
    // 제거하면서 part_id 선두 인덱스가 사라지므로 별도로 둔다. (V56 참고)
    indexes = [
        Index(name = "idx_interview_schedule_part_id", columnList = "part_id"),
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
