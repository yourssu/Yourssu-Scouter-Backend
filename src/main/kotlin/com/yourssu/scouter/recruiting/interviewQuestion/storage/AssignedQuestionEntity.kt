package com.yourssu.scouter.recruiting.interviewQuestion.storage

import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestion
import com.yourssu.scouter.recruiting.interviewQuestion.implement.AssignedQuestionCategory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "assigned_question")
class AssignedQuestionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "assigned_member_id", nullable = false)
    val assignedMemberId: Long,

    @Column(name = "applicant_id", nullable = false)
    val applicantId: Long,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: AssignedQuestionCategory,

    @Column(name = "source_question_id")
    val sourceQuestionId: Long?,

    @Column(columnDefinition = "TEXT")
    val content: String?,

    @Column(nullable = false)
    val sortOrder: Int,

    @Column(name = "is_selected")
    val isSelected: Boolean? = null,
) {

    fun toDomain(requirementIds: List<Long> = emptyList()): AssignedQuestion = AssignedQuestion(
        id = id,
        assignedMemberId = assignedMemberId,
        applicantId = applicantId,
        sourceQuestionId = sourceQuestionId,
        content = content,
        category = category,
        sortOrder = sortOrder,
        isSelected = isSelected,
        requirementIds = requirementIds,
    )
}
