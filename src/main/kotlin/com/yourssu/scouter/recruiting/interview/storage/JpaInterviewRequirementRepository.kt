package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.masterdata.semester.storage.SemesterEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface JpaInterviewRequirementRepository : JpaRepository<InterviewRequirementEntity, Long> {
    fun findAllByPartIdAndSemester(partId: Long, semester: SemesterEntity): List<InterviewRequirementEntity>

    fun findAllByPartIsNullAndSemester(semester: SemesterEntity): List<InterviewRequirementEntity>

    @Query(
        "SELECT r FROM InterviewRequirementEntity r " +
            "WHERE r.semester = :semester AND (r.part.id = :partId OR r.part IS NULL)",
    )
    fun findAllApplicableByPartIdAndSemester(
        @Param("partId") partId: Long,
        @Param("semester") semester: SemesterEntity,
    ): List<InterviewRequirementEntity>
}
