package com.yourssu.scouter.recruiting.applicant.implement

import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.semester.implement.Semester

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
