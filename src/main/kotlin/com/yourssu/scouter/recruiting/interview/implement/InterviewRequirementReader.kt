package com.yourssu.scouter.recruiting.interview.implement

import com.yourssu.scouter.common.semester.implement.Semester
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class InterviewRequirementReader(
    private val partInterviewRequirementRepository: InterviewRequirementRepository,
) {

    fun readAllByPartIdAndSemester(partId: Long, semester: Semester): List<InterviewRequirement> {
        return partInterviewRequirementRepository.findAllByPartIdAndSemester(partId, semester)
    }
}
