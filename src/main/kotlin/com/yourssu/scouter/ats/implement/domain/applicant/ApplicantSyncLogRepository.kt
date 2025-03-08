package com.yourssu.scouter.ats.implement.domain.applicant

interface ApplicantSyncLogRepository {

    fun saveAll(applicantSyncLogs: List<ApplicantSyncLog>)
    fun findAllByApplicantSemesterId(applicantSemesterId: Long): List<ApplicantSyncLog>
    fun findFirstByOrderBySyncTimeDesc(): ApplicantSyncLog?
}
