package com.yourssu.scouter.recruiting.applicant.storage

import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncLog
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantSyncLogRepository
import org.springframework.stereotype.Repository

@Repository
class ApplicantSyncLogRepositoryImpl(
    private val jpaApplicantSyncLogRepository: JpaApplicantSyncLogRepository,
): ApplicantSyncLogRepository {

    override fun saveAll(applicantSyncLogs: List<ApplicantSyncLog>) {
        jpaApplicantSyncLogRepository.saveAll(applicantSyncLogs.map { ApplicantSyncLogEntity.from(it) })
    }

    override fun findAllByApplicationSemesterId(applicationSemesterId: Long): List<ApplicantSyncLog> {
        return jpaApplicantSyncLogRepository.findAllByApplicationSemesterId(applicationSemesterId).map { it.toDomain() }
    }

    override fun findFirstByOrderBySyncTimeDesc(): ApplicantSyncLog? {
        return jpaApplicantSyncLogRepository.findFirstByOrderBySyncTimeDesc()?.toDomain()
    }
}
