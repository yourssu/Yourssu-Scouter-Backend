package com.yourssu.scouter.recruiting.support.business

import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.rubric.implement.RubricGroupType

interface InterviewRequirementLookup {
    fun findAllByPartIdAndSemester(partId: Long, semester: Semester): List<InterviewRequirementProfile>
}

data class InterviewRequirementProfile(
    val id: Long,
    val content: String,
    val rubricType: RubricGroupType,
)
