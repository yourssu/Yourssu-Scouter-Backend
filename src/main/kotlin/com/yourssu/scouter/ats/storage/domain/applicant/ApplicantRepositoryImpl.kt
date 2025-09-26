package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantRepository
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ApplicantRepositoryImpl(
    private val jpaApplicantRepository: JpaApplicantRepository,
) : ApplicantRepository {

    override fun save(applicant: Applicant): Applicant {
        return jpaApplicantRepository.save(ApplicantEntity.from(applicant)).toDomain()
    }

    override fun saveAll(applicants: List<Applicant>) {
        jpaApplicantRepository.saveAll(applicants.map { ApplicantEntity.from(it) })
    }

    override fun findById(applicantId: Long): Applicant? {
        return jpaApplicantRepository.findByIdOrNull(applicantId)?.toDomain()
    }

    override fun findAll(): List<Applicant> {
        return jpaApplicantRepository.findAll().map { it.toDomain() }
    }

    override fun findAllByState(state: ApplicantState): List<Applicant> {
        return jpaApplicantRepository.findAllByState(state).map { it.toDomain() }
    }

    override fun findAllByIdIn(applicantIds: List<Long>): List<Applicant> {
        return jpaApplicantRepository.findAllByIdIn(applicantIds).map { it.toDomain() }
    }

    override fun deleteById(applicantId: Long) {
        jpaApplicantRepository.deleteById(applicantId)
    }
}
