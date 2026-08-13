package com.yourssu.scouter.recruiting.applicant.business.dto

import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.masterdata.department.implement.Department
import com.yourssu.scouter.masterdata.part.implement.Part
import com.yourssu.scouter.masterdata.semester.implement.Semester
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
