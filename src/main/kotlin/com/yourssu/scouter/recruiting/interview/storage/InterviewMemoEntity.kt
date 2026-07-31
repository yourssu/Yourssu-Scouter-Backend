package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.recruiting.interview.implement.InterviewMemo
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "interview_memo")
class InterviewMemoEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "assigned_question_id", nullable = false)
    val assignedQuestionId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val memo: String,
) {

    fun toDomain(): InterviewMemo = InterviewMemo(
        id = id,
        assignedQuestionId = assignedQuestionId,
        memo = memo,
    )
}
