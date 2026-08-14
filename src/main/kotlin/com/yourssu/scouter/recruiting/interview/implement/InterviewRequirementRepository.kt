package com.yourssu.scouter.recruiting.interview.implement

import com.yourssu.scouter.masterdata.semester.implement.Semester

interface InterviewRequirementRepository {

    fun findAllByPartIdAndSemester(partId: Long, semester: Semester): List<InterviewRequirement>

    fun findAllGlobalBySemester(semester: Semester): List<InterviewRequirement>

    fun findAllApplicableByPartIdAndSemester(partId: Long, semester: Semester): List<InterviewRequirement>

    fun findAllByIdIn(ids: Collection<Long>): List<InterviewRequirement>

    fun saveAll(
        requirements: List<InterviewRequirement>,
        partId: Long,
        semester: Semester
    ): List<InterviewRequirement>

    fun saveAllGlobal(
        requirements: List<InterviewRequirement>,
        semester: Semester
    ): List<InterviewRequirement>
}
