package com.yourssu.scouter.ats.storage.domain.applicant

import org.springframework.data.jpa.repository.JpaRepository

interface JpaApplicantAnswerRepository : JpaRepository<ApplicantAnswerEntity, Long> {
    fun findAllByApplicantId(applicantId: Long): List<ApplicantAnswerEntity>
}
