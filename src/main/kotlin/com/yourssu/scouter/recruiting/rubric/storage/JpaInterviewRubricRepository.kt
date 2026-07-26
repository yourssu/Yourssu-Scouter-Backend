package com.yourssu.scouter.recruiting.rubric.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaInterviewRubricRepository : JpaRepository<InterviewRubricEntity, Long> {

    fun findByPartIdAndSemester(partId: Long, semester: String): InterviewRubricEntity?
}
