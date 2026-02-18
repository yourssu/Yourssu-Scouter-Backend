package com.yourssu.scouter.ats.implement.domain.applicant

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester

class ApplicantSyncMapping(
    val id: Long? = null,
    val applicationSemester: Semester,
    val part: Part,
    val formId: String,
    val nameQuestion: String?,
    val emailQuestion: String?,
    val phoneNumberQuestion: String?,
    val ageQuestion: String?,
    val departmentQuestion: String?,
    val studentIdQuestion: String,
    val academicSemesterQuestion: String?,
    val availableTimeQuestion: String?,
) {
}
