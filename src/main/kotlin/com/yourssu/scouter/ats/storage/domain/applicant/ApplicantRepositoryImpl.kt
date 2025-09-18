package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantRepository
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class ApplicantRepositoryImpl(
    private val jpaApplicantRepository: JpaApplicantRepository,
    private val jpaAvailableTimeRepository: JpaApplicantAvailableTimeRepository,
) : ApplicantRepository {

    override fun save(applicant: Applicant): Applicant {
        val availableTimeEntities = jpaAvailableTimeRepository
            .saveAll(ApplicantAvailableTimeEntity.from(applicant))

        val availableTimes: List<LocalDateTime> = ApplicantAvailableTimeEntity
            .toDomains(availableTimeEntities)

        return jpaApplicantRepository.save(ApplicantEntity.from(applicant)).toDomain(availableTimes)
    }

    override fun saveAll(applicants: List<Applicant>) {
        jpaAvailableTimeRepository
            .saveAll(applicants.map { ApplicantAvailableTimeEntity.from(it) }.flatten())

        jpaApplicantRepository.saveAll(applicants.map { ApplicantEntity.from(it) })
    }

    override fun findById(applicantId: Long): Applicant? {
        val availableTimeEntities = jpaAvailableTimeRepository.findAllByApplicantId(applicantId)
        return jpaApplicantRepository.findByIdOrNull(applicantId)
            ?.toDomain(ApplicantAvailableTimeEntity.toDomains(availableTimeEntities))
    }

    override fun findAll(): List<Applicant> {

        return jpaApplicantRepository.findAll().map {
            val availableTimeEntities = jpaAvailableTimeRepository.findAllByApplicantId(it.id!!)
            it.toDomain(ApplicantAvailableTimeEntity.toDomains(availableTimeEntities))
        }
    }

    override fun findAllByState(state: ApplicantState): List<Applicant> {
        return jpaApplicantRepository.findAllByState(state).map {
            val availableTimeEntities = jpaAvailableTimeRepository.findAllByApplicantId(it.id!!)
            it.toDomain(ApplicantAvailableTimeEntity.toDomains(availableTimeEntities))
        }
    }

    override fun deleteById(applicantId: Long) {
        jpaAvailableTimeRepository.deleteAllByApplicantId(applicantId)
        jpaApplicantRepository.deleteById(applicantId)
    }
}
