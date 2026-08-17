package com.yourssu.scouter.recruiting.interviewQuestion.storage

import com.yourssu.scouter.recruiting.interviewQuestion.implement.Question
import com.yourssu.scouter.recruiting.interviewQuestion.implement.QuestionCategory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "question")
class QuestionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "part_id")
    val partId: Long? = null,

    /** CULTURE / PART 질문은 학기별로 관리됩니다. INTRO / OUTRO 는 학기에 무관하게 공유됩니다. */
    @Column(name = "semester_id")
    val semesterId: Long? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: QuestionCategory,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(nullable = false)
    val sortOrder: Int,
) {

    fun toDomain(requirementIds: List<Long> = emptyList()): Question = Question(
        id = id,
        partId = partId,
        semesterId = semesterId,
        category = category,
        content = content,
        sortOrder = sortOrder,
        requirementIds = requirementIds,
    )
}
