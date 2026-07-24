package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.recruiting.interview.implement.InterviewScript
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "interview_script")
class InterviewScriptEntity(

    @Id
    @Column(name = "part_id")
    val partId: Long,

    @Column(nullable = false, columnDefinition = "TEXT")
    val opening: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    val closing: String,
) {

    fun toDomain(): InterviewScript = InterviewScript(
        partId = partId,
        opening = opening,
        closing = closing,
    )
}
