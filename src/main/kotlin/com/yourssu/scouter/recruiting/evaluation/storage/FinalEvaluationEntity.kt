package com.yourssu.scouter.recruiting.evaluation.storage

import com.yourssu.scouter.recruiting.applicant.storage.ApplicantEntity
import com.yourssu.scouter.recruiting.evaluation.implement.InterviewResult
import com.yourssu.scouter.recruiting.rubric.storage.InterviewRubricEntity
import com.yourssu.scouter.auth.user.storage.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "final_evaluation")
class FinalEvaluationEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_rubric_id", nullable = false)
    val interviewRubric: InterviewRubricEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    val applicant: ApplicantEntity,

    @Column(name = "overall_comment", columnDefinition = "TEXT")
    var overallComment: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "interview_result", nullable = false)
    var interviewResult: InterviewResult = InterviewResult.PENDING,

    @Column(nullable = false)
    var score: Int,

    @Column(nullable = false)
    var submit: Boolean = false,

    @Column(name = "submitted_at")
    var submittedAt: LocalDateTime? = null
) {
    fun update(overallComment: String?, interviewResult: InterviewResult, score: Int, submit: Boolean, submittedAt: LocalDateTime?) {
        this.overallComment = overallComment
        this.interviewResult = interviewResult
        this.score = score
        this.submit = submit
        this.submittedAt = submittedAt
    }
}

