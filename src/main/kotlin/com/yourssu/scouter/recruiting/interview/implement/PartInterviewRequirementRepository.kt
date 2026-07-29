package com.yourssu.scouter.recruiting.interview.implement

import com.yourssu.scouter.common.semester.implement.Semester

interface PartInterviewRequirementRepository {

    fun findAllByPartIdAndSemester(partId: Long, semester: Semester): List<PartInterviewRequirement>

    fun saveAll(
        requirements: List<PartInterviewRequirement>,
        partId: Long,
        semester: Semester
    ): List<PartInterviewRequirement>
}
