package com.yourssu.scouter.recruiting.interview.implement

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class PartInterviewRequirementReader(
    private val partInterviewRequirementRepository: PartInterviewRequirementRepository,
) {

    fun readByPartId(partId: Long): PartInterviewRequirement? {
        return partInterviewRequirementRepository.findByPartId(partId)
    }
}
