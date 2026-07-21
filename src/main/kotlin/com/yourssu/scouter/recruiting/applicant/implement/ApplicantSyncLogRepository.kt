package com.yourssu.scouter.recruiting.applicant.implement

interface ApplicantSyncLogRepository {

    fun saveAll(applicantSyncLogs: List<ApplicantSyncLog>)
    fun findAllByApplicationSemesterId(applicationSemesterId: Long): List<ApplicantSyncLog>
    fun findFirstByOrderBySyncTimeDesc(): ApplicantSyncLog?
}
