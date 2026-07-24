package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirement
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "part_interview_requirement")
class PartInterviewRequirementEntity(

    @Id
    @Column(name = "part_id")
    val partId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val culture: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val team: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val job: String,
) {

    fun toDomain(): PartInterviewRequirement = PartInterviewRequirement(
        partId = partId,
        culture = culture,
        team = team,
        job = job,
    )
}
