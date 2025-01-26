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

    override fun save(applicant: Applicant): Applicant =
        jpaApplicantRepository.save(ApplicantEntity.from(applicant)).toDomain()

    override fun findById(applicantId: Long): Applicant? {
        return jpaApplicantRepository.findByIdOrNull(applicantId)?.toDomain()
    }

    override fun findAll(): List<Applicant> = jpaApplicantRepository.findAll().map { it.toDomain() }

    override fun findAllByName(name: String): List<Applicant> =
        jpaApplicantRepository.findAllByName(name).map { it.toDomain() }

    override fun findAllByState(state: ApplicantState): List<Applicant> =
        jpaApplicantRepository.findAllByState(state).map { it.toDomain() }

    override fun findAllBySemesterId(semesterId: Long): List<Applicant> =
        jpaApplicantRepository.findAllByApplicationSemesterId(semesterId).map { it.toDomain() }

    override fun deleteById(applicantId: Long) {
        jpaApplicantRepository.deleteById(applicantId)
    }
}
