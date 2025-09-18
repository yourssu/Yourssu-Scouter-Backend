package com.yourssu.scouter.ats.storage.domain.applicant

import org.springframework.data.jpa.repository.JpaRepository


interface JpaApplicantAvailableTimeRepository : JpaRepository<ApplicantAvailableTimeEntity, Long> {

    fun findAllByApplicantId(applicantId: Long): List<ApplicantAvailableTimeEntity>
    fun deleteAllByApplicantId(applicantId: Long)
}