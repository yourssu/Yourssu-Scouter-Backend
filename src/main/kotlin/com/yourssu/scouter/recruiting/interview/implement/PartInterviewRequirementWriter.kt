package com.yourssu.scouter.recruiting.interview.implement

import com.yourssu.scouter.common.semester.implement.Semester
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class PartInterviewRequirementWriter(
    private val partInterviewRequirementRepository: PartInterviewRequirementRepository,
) {

    fun saveAll(
        requirements: List<PartInterviewRequirement>,
        partId: Long,
        semester: Semester
    ): List<PartInterviewRequirement> {
        return partInterviewRequirementRepository.saveAll(requirements, partId, semester)
    }
}
