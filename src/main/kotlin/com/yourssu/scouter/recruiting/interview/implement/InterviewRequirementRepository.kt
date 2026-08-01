package com.yourssu.scouter.recruiting.interview.implement

import com.yourssu.scouter.common.semester.implement.Semester

interface InterviewRequirementRepository {

    fun findAllByPartIdAndSemester(partId: Long, semester: Semester): List<InterviewRequirement>

    fun saveAll(
        requirements: List<InterviewRequirement>,
        partId: Long,
        semester: Semester
    ): List<InterviewRequirement>
}
