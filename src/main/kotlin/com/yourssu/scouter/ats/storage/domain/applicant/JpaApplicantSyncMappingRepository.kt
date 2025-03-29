package com.yourssu.scouter.ats.storage.domain.applicant

import org.springframework.data.jpa.repository.JpaRepository

interface JpaApplicantSyncMappingRepository : JpaRepository<ApplicantSyncMappingEntity, Long> {

    fun findAllByApplicantSemesterId(applicantSemesterId: Long): List<ApplicantSyncMappingEntity>
}
