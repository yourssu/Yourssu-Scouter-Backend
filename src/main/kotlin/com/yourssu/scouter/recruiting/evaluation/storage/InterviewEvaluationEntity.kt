package com.yourssu.scouter.recruiting.evaluation.storage

import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import jakarta.persistence.*
import java.time.Instant

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
    var result: InterviewResult = InterviewResult.PENDING,

    @Column(name = "submitted_at")
    var submittedAt: Instant? = null
) {
    fun updateEvaluation(newScore: Int, newResult: InterviewResult, newSubmittedAt: Instant?) {
        this.score = newScore
        this.result = newResult
        this.submittedAt = newSubmittedAt
    }
}
