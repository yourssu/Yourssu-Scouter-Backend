package com.yourssu.scouter.recruiting.rubric.storage

import com.yourssu.scouter.common.semester.storage.SemesterEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JpaInterviewRubricRepository : JpaRepository<InterviewRubricEntity, Long> {

    fun findByPartIdAndSemester(partId: Long, semester: SemesterEntity): InterviewRubricEntity?
}
