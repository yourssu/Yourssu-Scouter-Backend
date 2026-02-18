package com.yourssu.scouter.ats.storage.domain.applicant

import org.springframework.data.jpa.repository.JpaRepository

interface JpaApplicantSyncLogRepository : JpaRepository<ApplicantSyncLogEntity, Long> {

    fun findAllByApplicationSemesterId(applicationSemesterId: Long): List<ApplicantSyncLogEntity>

    fun findFirstByOrderBySyncTimeDesc(): ApplicantSyncLogEntity?
}
