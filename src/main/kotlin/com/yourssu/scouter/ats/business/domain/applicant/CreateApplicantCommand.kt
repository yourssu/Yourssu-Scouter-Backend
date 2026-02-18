package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.semester.Semester
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

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
    val applicationSemesterId: Long,
    val academicSemester: String,
    val availableTimes: List<Instant>,
) {

    fun toDomain(
        department: Department,
        part: Part,
        applicationSemester: Semester
    ): Applicant = Applicant(
        name = name,
        email = email,
        phoneNumber = phoneNumber,
        age = age,
        department = department.name,
        studentId = studentId,
        part = part,
        state = state,
        applicationDateTime = applicationDate.atStartOfDay().toInstant(ZoneOffset.UTC),
        applicationSemester = applicationSemester,
        academicSemester = academicSemester,
        availableTimes = availableTimes,
    )
}
