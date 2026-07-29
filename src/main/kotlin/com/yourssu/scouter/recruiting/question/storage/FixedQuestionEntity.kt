package com.yourssu.scouter.recruiting.question.storage

import com.yourssu.scouter.recruiting.question.implement.FixedQuestion
import com.yourssu.scouter.recruiting.question.implement.FixedQuestionCategory
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "fixed_question")
class FixedQuestionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val category: FixedQuestionCategory,

    @Column(nullable = false, columnDefinition = "TEXT")
    val content: String,

    @Column(nullable = false)
    val sortOrder: Int,
) {

    fun toDomain(): FixedQuestion = FixedQuestion(
        id = id,
        category = category,
        content = content,
        sortOrder = sortOrder,
    )
}
