package com.yourssu.scouter.recruiting.interview.implement

import com.yourssu.scouter.masterdata.semester.implement.Semester
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional
class InterviewRequirementWriter(
    private val partInterviewRequirementRepository: InterviewRequirementRepository,
) {

    fun saveAll(
        requirements: List<InterviewRequirement>,
        partId: Long,
        semester: Semester
    ): List<InterviewRequirement> {
        return partInterviewRequirementRepository.saveAll(requirements, partId, semester)
    }

    fun saveAllGlobal(
        requirements: List<InterviewRequirement>,
        semester: Semester
    ): List<InterviewRequirement> {
        return partInterviewRequirementRepository.saveAllGlobal(requirements, semester)
    }
}
