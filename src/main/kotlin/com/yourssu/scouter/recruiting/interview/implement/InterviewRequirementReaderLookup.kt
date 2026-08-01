package com.yourssu.scouter.recruiting.interview.implement

import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementLookup
import com.yourssu.scouter.recruiting.support.business.InterviewRequirementProfile
import org.springframework.stereotype.Component

@Component
class InterviewRequirementReaderLookup(
    private val interviewRequirementReader: InterviewRequirementReader,
) : InterviewRequirementLookup {

    override fun findAllByPartIdAndSemester(partId: Long, semester: Semester): List<InterviewRequirementProfile> {
        return interviewRequirementReader
            .readAllByPartIdAndSemester(partId, semester)
            .map { InterviewRequirementProfile(id = it.id!!, content = it.content, rubricType = it.rubricType) }
    }
}
