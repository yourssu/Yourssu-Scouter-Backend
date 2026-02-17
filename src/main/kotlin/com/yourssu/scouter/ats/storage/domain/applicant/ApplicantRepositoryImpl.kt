package com.yourssu.scouter.ats.storage.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantRepository
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ApplicantRepositoryImpl(
    private val jpaApplicantRepository: JpaApplicantRepository,
    private val jpaAvailableTimeRepository: JpaApplicantAvailableTimeRepository,
) : ApplicantRepository {

    override fun save(applicant: Applicant): Applicant {
        val savedApplicant =
            jpaApplicantRepository.save(ApplicantEntity.from(applicant)).toDomain(applicant.availableTimes)

        jpaAvailableTimeRepository.saveAll(ApplicantAvailableTimeEntity.from(savedApplicant))

        return savedApplicant
    }

    override fun saveAll(applicants: List<Applicant>) {
        val savedApplicants = jpaApplicantRepository
            .saveAll(applicants.map { ApplicantEntity.from(it) })
        val applicantMap = applicants.associateBy { "${it.name}_${it.email}" }
        val availableTimeEntities = savedApplicants.flatMap { saved ->
            val originalTime = applicantMap["${saved.name}_${saved.email}"]?.availableTimes ?: emptyList()
            ApplicantAvailableTimeEntity.from(saved.toDomain(originalTime))
        }

        jpaAvailableTimeRepository
            .saveAll(availableTimeEntities)
    }

    override fun findById(applicantId: Long): Applicant? {
        val availableTimeEntities = jpaAvailableTimeRepository.findAllByApplicantId(applicantId)
        return jpaApplicantRepository.findByIdOrNull(applicantId)
            ?.toDomain(ApplicantAvailableTimeEntity.toDomains(availableTimeEntities))
    }

    override fun findAllByPartId(partId: Long): List<Applicant> {
        return findApplicantsWithAvailableTimes(jpaApplicantRepository.findAllByPartId(partId))
    }

    override fun findAllByPartIdAndState(partId: Long, state: ApplicantState): List<Applicant> {
        return findApplicantsWithAvailableTimes(
            jpaApplicantRepository.findAllByPartIdAndState(partId, state)
        )
    }

    override fun findAll(): List<Applicant> {
        return findApplicantsWithAvailableTimes(jpaApplicantRepository.findAll())
    }

    override fun findAllByState(state: ApplicantState): List<Applicant> {
        return findApplicantsWithAvailableTimes(jpaApplicantRepository.findAllByState(state))
    }

    private fun findApplicantsWithAvailableTimes(applicantEntities: List<ApplicantEntity>): List<Applicant> {
        val applicantIds = applicantEntities.mapNotNull { it.id }

        val availableTimeEntities = jpaAvailableTimeRepository.findAllInApplicantId(applicantIds)
        val availableTimeMap = ApplicantAvailableTimeEntity.groupByApplicantId(availableTimeEntities)

        return applicantEntities.map { entity ->
            val availableTimes = availableTimeMap[entity.id] ?: emptyList()
            entity.toDomain(availableTimes)
        }
    }

    override fun findAllByIdInWithoutAvailableTimes(applicantIds: List<Long>): List<Applicant> {
        return jpaApplicantRepository.findAllByIdIn(applicantIds).map { it.toDomain(emptyList()) }
    }

    override fun deleteById(applicantId: Long) {
        jpaAvailableTimeRepository.deleteAllByApplicantId(applicantId)
        jpaApplicantRepository.deleteById(applicantId)
    }
}
