package com.yourssu.scouter.recruiting.applicant.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaApplicantAnswerRepository : JpaRepository<ApplicantAnswerEntity, Long> {
    fun findAllByApplicantId(applicantId: Long): List<ApplicantAnswerEntity>
}
