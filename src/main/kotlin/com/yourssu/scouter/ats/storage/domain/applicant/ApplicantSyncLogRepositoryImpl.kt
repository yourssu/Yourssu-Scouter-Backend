package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncLog
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantSyncLogRepository
import org.springframework.stereotype.Repository

@Repository
class ApplicantSyncLogRepositoryImpl(
    private val jpaApplicantSyncLogRepository: JpaApplicantSyncLogRepository,
): ApplicantSyncLogRepository {

    override fun saveAll(applicantSyncLogs: List<ApplicantSyncLog>) {
        jpaApplicantSyncLogRepository.saveAll(applicantSyncLogs.map { ApplicantSyncLogEntity.from(it) })
    }

    override fun findAllByApplicantSemesterId(applicantSemesterId: Long): List<ApplicantSyncLog> {
        return jpaApplicantSyncLogRepository.findAllByApplicantSemesterId(applicantSemesterId).map { it.toDomain() }
    }

    override fun findFirstByOrderBySyncTimeDesc(): ApplicantSyncLog? {
        return jpaApplicantSyncLogRepository.findFirstByOrderBySyncTimeDesc()?.toDomain()
    }
}
