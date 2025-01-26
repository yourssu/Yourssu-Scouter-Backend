package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import java.time.LocalDate

data class CreateApplicantCommand(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val age: String,
    val departmentId: Long,
    val studentId: String,
    val partId: Long,
    val state: ApplicantState,
    val applicationDate: LocalDate,
    val applicantSemesterId: Long,
) {

    fun toDomain(
        department: Department,
        part: Part,
        applicantSemester: Semester
    ): Applicant = Applicant(
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        age = age,
        department = department,
        studentId = studentId,
        part = part,
        state = state,
        applicationDate = applicationDate,
        applicationSemester = applicantSemester,
    )
}
