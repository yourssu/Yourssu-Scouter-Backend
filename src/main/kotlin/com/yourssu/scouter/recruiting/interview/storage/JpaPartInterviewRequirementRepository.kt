package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.common.semester.storage.SemesterEntity
import org.springframework.data.jpa.repository.JpaRepository

interface JpaPartInterviewRequirementRepository : JpaRepository<PartInterviewRequirementEntity, Long> {
    fun findAllByPartIdAndSemester(partId: Long, semester: SemesterEntity): List<PartInterviewRequirementEntity>
}
