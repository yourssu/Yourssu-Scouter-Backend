package com.yourssu.scouter.ats.storage.domain.applicant

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query


interface JpaApplicantAvailableTimeRepository : JpaRepository<ApplicantAvailableTimeEntity, Long> {

    @Query("SELECT aat FROM ApplicantAvailableTimeEntity aat WHERE aat.applicant.id IN :applicantId")
    fun findAllInApplicantId(applicantId: List<Long>): List<ApplicantAvailableTimeEntity>
    fun findAllByApplicantId(applicantId: Long): List<ApplicantAvailableTimeEntity>
    fun deleteAllByApplicantId(applicantId: Long)
}