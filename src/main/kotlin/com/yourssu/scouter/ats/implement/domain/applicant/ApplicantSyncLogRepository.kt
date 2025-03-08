package com.yourssu.scouter.ats.implement.domain.applicant

interface ApplicantSyncLogRepository {

    fun save(applicantSyncLog: ApplicantSyncLog): ApplicantSyncLog

    fun findAllByApplicantSemesterId(applicantSemesterId: Long): List<ApplicantSyncLog>
}
