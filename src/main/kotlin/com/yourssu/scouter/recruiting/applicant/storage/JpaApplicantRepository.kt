package com.yourssu.scouter.recruiting.applicant.storage

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.recruiting.applicant.implement.AssignmentResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface JpaApplicantRepository : JpaRepository<ApplicantEntity, Long> {

    fun findAllByName(name: String): List<ApplicantEntity>
    fun findAllByState(state: ApplicantState): List<ApplicantEntity>
    fun findAllByApplicationSemesterId(semesterId: Long): List<ApplicantEntity>
    fun findAllByIdIn(ids: List<Long>): List<ApplicantEntity>
    fun findAllByPartId(partId: Long): List<ApplicantEntity>
    fun findAllByPartIdAndState(partId: Long, state: ApplicantState): List<ApplicantEntity>
    fun findAllByEmailIn(emails: List<String>): List<ApplicantEntity>

    @Modifying
    @Query("UPDATE ApplicantEntity a SET a.assignmentResult = :assignmentResult WHERE a.id = :applicantId")
    fun updateAssignmentResult(applicantId: Long, assignmentResult: AssignmentResult)
}
