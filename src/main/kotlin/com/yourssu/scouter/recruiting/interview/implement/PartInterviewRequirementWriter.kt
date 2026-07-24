package com.yourssu.scouter.recruiting.interview.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class PartInterviewRequirementWriter(
    private val partInterviewRequirementRepository: PartInterviewRequirementRepository,
) {

    fun upsert(requirement: PartInterviewRequirement): PartInterviewRequirement {
        return partInterviewRequirementRepository.upsert(requirement)
    }
}
