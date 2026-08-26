package com.yourssu.scouter.recruiting.applicant.implement

import com.yourssu.scouter.recruiting.applicant.storage.ApplicantSyncMappingEntity
import com.yourssu.scouter.recruiting.applicant.storage.JpaApplicantSyncMappingRepository
import org.springframework.stereotype.Repository

@Repository
class ApplicantSyncMappingRepositoryImpl(
    private val jpaApplicantSyncMappingRepository: JpaApplicantSyncMappingRepository,
) : ApplicantSyncMappingRepository {

    override fun save(applicantSyncMapping: ApplicantSyncMapping) {
        jpaApplicantSyncMappingRepository.save(ApplicantSyncMappingEntity.from(applicantSyncMapping))
    }

    override fun findAllByApplicationSemesterId(semesterId: Long): List<ApplicantSyncMapping> {
        return jpaApplicantSyncMappingRepository.findAllByApplicationSemesterId(semesterId).map { it.toDomain() }
    }

    override fun findByFormId(formId: String): ApplicantSyncMapping? {
        return jpaApplicantSyncMappingRepository.findByFormId(formId)?.toDomain()
    }

    override fun count(): Long {
        return jpaApplicantSyncMappingRepository.count()
    }

    override fun existsByApplicationSemesterIdAndPartId(applicationSemesterId: Long, partId: Long): Boolean {
        return jpaApplicantSyncMappingRepository.existsByApplicationSemester_IdAndPart_Id(applicationSemesterId, partId)
    }
}
