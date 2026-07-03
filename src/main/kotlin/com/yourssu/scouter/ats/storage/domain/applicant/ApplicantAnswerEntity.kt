package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantAnswer
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.ForeignKey
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction

/**
 * 지원자 필드로 매핑되지 않은 폼의 텍스트 기반 질문/응답을 담는 Entity Class
 * @see ApplicantEntity
 */
@Entity
@Table(name = "applicant_answer")
class ApplicantAnswerEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false, foreignKey = ForeignKey(name = "fk_applicant_answer_applicant"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    val applicant: ApplicantEntity,

    @Column(nullable = false, columnDefinition = "TEXT")
    val question: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val answer: String,
) {
    fun toDomain(): ApplicantAnswer = ApplicantAnswer(
        id = id,
        applicantId = applicant.id!!,
        question = question,
        answer = answer,
    )
}
