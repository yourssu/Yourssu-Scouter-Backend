package com.yourssu.scouter.recruiting.evaluation.storage

import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType
import com.yourssu.scouter.recruiting.rubric.storage.InterviewRubricEntity
import com.yourssu.scouter.recruiting.interview.storage.InterviewRequirementEntity
import jakarta.persistence.*

@Entity
@Table(name = "interview_evaluation_item")
class InterviewEvaluationItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_rubric_id", nullable = false)
    var interviewRubric: InterviewRubricEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_requirement_id")
    var interviewRequirement: InterviewRequirementEntity?,

    @Column(nullable = false)
    var keyword: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "rubric_type", nullable = false)
    var rubricType: RubricGroupType,

    @Column(name = "max_score", nullable = false)
    var maxScore: Int
)
