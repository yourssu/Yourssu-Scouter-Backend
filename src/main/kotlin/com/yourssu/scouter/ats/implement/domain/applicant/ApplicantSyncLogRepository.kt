package com.yourssu.scouter.ats.implement.domain.applicant

interface ApplicantSyncLogRepository {

    fun saveAll(applicantSyncLogs: List<ApplicantSyncLog>)
    fun findAllByApplicationSemesterId(applicationSemesterId: Long): List<ApplicantSyncLog>
    fun findFirstByOrderBySyncTimeDesc(): ApplicantSyncLog?
}
