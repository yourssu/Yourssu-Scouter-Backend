package com.yourssu.scouter.ats.implement.domain.applicant

import com.yourssu.scouter.ats.storage.domain.applicant.ApplicantSyncMappingEntity
import com.yourssu.scouter.ats.storage.domain.applicant.JpaApplicantSyncMappingRepository
import org.springframework.stereotype.Repository

@Repository
class ApplicantSyncMappingRepositoryImpl(
    private val jpaApplicantSyncMappingRepository: JpaApplicantSyncMappingRepository,
) : ApplicantSyncMappingRepository {

    override fun save(applicantSyncMapping: ApplicantSyncMapping) {
        jpaApplicantSyncMappingRepository.save(ApplicantSyncMappingEntity.from(applicantSyncMapping))
    }

    override fun findAllByApplicantSemesterId(semesterId: Long): List<ApplicantSyncMapping> {
        return jpaApplicantSyncMappingRepository.findAllByApplicantSemesterId(semesterId).map { it.toDomain() }
    }

    override fun count(): Long {
        return jpaApplicantSyncMappingRepository.count()
    }
}
