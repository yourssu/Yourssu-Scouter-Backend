package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.common.semester.storage.SemesterEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JpaInterviewRequirementRepository : JpaRepository<InterviewRequirementEntity, Long> {
    fun findAllByPartIdAndSemester(partId: Long, semester: SemesterEntity): List<InterviewRequirementEntity>
}
