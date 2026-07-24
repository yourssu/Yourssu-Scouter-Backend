package com.yourssu.scouter.recruiting.interview.storage

import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirement
import com.yourssu.scouter.recruiting.interview.implement.PartInterviewRequirementRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class PartInterviewRequirementRepositoryImpl(
    private val jpaPartInterviewRequirementRepository: JpaPartInterviewRequirementRepository,
) : PartInterviewRequirementRepository {

    override fun findByPartId(partId: Long): PartInterviewRequirement? {
        return jpaPartInterviewRequirementRepository.findByIdOrNull(partId)?.toDomain()
    }

    override fun upsert(requirement: PartInterviewRequirement): PartInterviewRequirement {
        val entity = PartInterviewRequirementEntity(
            partId = requirement.partId,
            culture = requirement.culture,
            team = requirement.team,
            job = requirement.job,
        )

        return jpaPartInterviewRequirementRepository.save(entity).toDomain()
    }
}
