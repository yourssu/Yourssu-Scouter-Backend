package com.yourssu.scouter.recruiting.interviewQuestion.storage

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.IdClass
import jakarta.persistence.Table
import java.io.Serializable

@Entity
@Table(name = "part_culture_question_selection")
@IdClass(PartCultureQuestionSelectionId::class)
class PartCultureQuestionSelectionEntity(

    @Id
    @Column(name = "part_id")
    val partId: Long,

    @Id
    @Column(name = "semester_id")
    val semesterId: Long,

    @Id
    @Column(name = "question_id")
    val questionId: Long,
)

data class PartCultureQuestionSelectionId(
    val partId: Long = 0,
    val semesterId: Long = 0,
    val questionId: Long = 0,
) : Serializable
