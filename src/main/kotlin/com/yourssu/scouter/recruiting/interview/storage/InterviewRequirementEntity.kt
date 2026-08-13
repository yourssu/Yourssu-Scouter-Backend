package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.masterdata.part.storage.PartEntity
import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import com.yourssu.scouter.recruiting.interview.implement.InterviewRequirement
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import jakarta.persistence.*

@Entity
@Table(name = "part_interview_requirement")
class InterviewRequirementEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "part_id", nullable = true)
    val part: PartEntity?,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "semester_id", nullable = false)
    val semester: SemesterEntity,

    @Column(name = "rubric_type", nullable = false)
    @Enumerated(EnumType.STRING)
    val rubricType: RubricGroupType,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,
) {
    fun toDomain(): InterviewRequirement = InterviewRequirement(
        id = id,
        partId = part?.id,
        semesterId = semester.id!!,
        rubricType = rubricType,
        content = content,
    )
}
