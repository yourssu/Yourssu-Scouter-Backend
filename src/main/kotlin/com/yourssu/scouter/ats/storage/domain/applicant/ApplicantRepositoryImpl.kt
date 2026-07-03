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

        jpaAvailableTimeRepository.deleteAllByApplicantId(savedApplicant.id!!)
        jpaAvailableTimeRepository.saveAll(ApplicantAvailableTimeEntity.from(savedApplicant))

        return savedApplicant
    }

    override fun saveAll(applicants: List<Applicant>): List<Applicant> {
        // JPA saveAll은 입력 순서를 보존하므로 zip으로 저장 전 도메인과 짝지어 복원한다.
        val savedEntities = jpaApplicantRepository.saveAll(applicants.map { ApplicantEntity.from(it) })
        val savedApplicants = savedEntities.zip(applicants) { entity, original ->
            entity.toDomain(original.availableTimes)
        }

        jpaAvailableTimeRepository.saveAll(savedApplicants.flatMap(ApplicantAvailableTimeEntity::from))

        return savedApplicants
    }

    override fun findById(applicantId: Long): Applicant? {
        val availableTimeEntities = jpaAvailableTimeRepository.findAllByApplicantId(applicantId)
        return jpaApplicantRepository.findByIdOrNull(applicantId)
            ?.toDomain(ApplicantAvailableTimeEntity.toDomains(availableTimeEntities))
    }

    override fun findAllByPartId(partId: Long): List<Applicant> {
        return findApplicantsWithAvailableTimes(jpaApplicantRepository.findAllByPartId(partId))
    }

    override fun findAllByPartIdAndState(
        partId: Long,
        state: ApplicantState,
    ): List<Applicant> {
        return findApplicantsWithAvailableTimes(
            jpaApplicantRepository.findAllByPartIdAndState(partId, state),
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

    override fun findAllByIdIn(applicantIds: List<Long>): List<Applicant> {
        return jpaApplicantRepository.findAllByIdIn(applicantIds).map { it.toDomain(emptyList()) }
    }

    override fun findAllByEmailIn(emails: List<String>): List<Applicant> {
        return jpaApplicantRepository.findAllByEmailIn(emails).map { it.toDomain(emptyList()) }
    }

    override fun deleteById(applicantId: Long) {
        jpaAvailableTimeRepository.deleteAllByApplicantId(applicantId)
        jpaApplicantRepository.deleteById(applicantId)
    }
}
