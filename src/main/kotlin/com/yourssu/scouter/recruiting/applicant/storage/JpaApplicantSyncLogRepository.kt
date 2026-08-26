package com.yourssu.scouter.recruiting.applicant.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaApplicantSyncLogRepository : JpaRepository<ApplicantSyncLogEntity, Long> {

    fun findAllByApplicationSemesterId(applicationSemesterId: Long): List<ApplicantSyncLogEntity>

    fun findFirstByOrderBySyncTimeDesc(): ApplicantSyncLogEntity?
}
