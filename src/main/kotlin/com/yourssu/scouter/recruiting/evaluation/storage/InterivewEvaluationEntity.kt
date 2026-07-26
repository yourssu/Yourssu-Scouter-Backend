package com.yourssu.scouter.recruiting.evaluation.storage

import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import jakarta.persistence.*

@Entity
@Table(name = "interview_evaluation")
class InterviewEvaluationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @Column(name = "applicant_id", nullable = false)
    val applicantId: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_evaluation_item_id", nullable = false)
    var interviewEvaluationItem: InterviewEvaluationItemEntity,

    @Column(name = "member_id", nullable = false)
    val memberId: Long, // 평가자 ID

    @Column(nullable = false)
    var score: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var result: InterviewResult = InterviewResult.PENDING

) {
    fun updateEvaluation(newScore: Int, newResult: InterviewResult) {
        this.score = newScore
        this.result = newResult
    }
}