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
        category = category,
        content = content,
        sortOrder = sortOrder,
        requirementIds = requirementIds,
    )
}
