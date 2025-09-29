package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 지원자가 면접 가능한 시간을 담는 Entity Class
 * @see ApplicantEntity
 */
@Entity
@Table(name = "applicant_available_time")
class ApplicantAvailableTimeEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    val applicant: ApplicantEntity,

    @Column(nullable = false)
    val availableTime: LocalDateTime,
) {
    companion object {
        fun from(applicant: Applicant): List<ApplicantAvailableTimeEntity> {
            return applicant.availableTimes
                .map { ApplicantAvailableTimeEntity(applicant = ApplicantEntity.from(applicant), availableTime = it) }
        }

        fun toDomains(availableTimes: List<ApplicantAvailableTimeEntity>): List<LocalDateTime> {
            return availableTimes.map { it.availableTime }
        }

        fun groupByApplicantId(availableTimes: List<ApplicantAvailableTimeEntity>): Map<Long, List<LocalDateTime>> {
            return availableTimes.groupBy(
                keySelector = { it.applicant.id!! },
                valueTransform = { it.availableTime }
            )
        }
    }
}