package com.yourssu.scouter.recruiting.interview.implement

import com.yourssu.scouter.common.semester.implement.Semester
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class PartInterviewRequirementReader(
    private val partInterviewRequirementRepository: PartInterviewRequirementRepository,
) {

    fun readAllByPartIdAndSemester(partId: Long, semester: Semester): List<PartInterviewRequirement> {
        return partInterviewRequirementRepository.findAllByPartIdAndSemester(partId, semester)
    }
}
