package com.yourssu.scouter.recruiting.applicant.business.dto

import com.yourssu.scouter.recruiting.applicant.implement.Applicant
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import com.yourssu.scouter.masterdata.part.business.dto.PartDto
import com.yourssu.scouter.masterdata.semester.business.dto.SemesterDto
import java.time.Instant

data class ApplicantDto(
    val id: Long,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val age: String,
    val department: String,
    val studentId: String,
    val part: PartDto,
    val state: ApplicantState,
    val applicationDateTime: Instant,
    val applicationSemester: SemesterDto,
    val academicSemester: String,
    val availableTimes: List<Instant>,
    val documentAverageScore: Double? = null,
    val interviewAverageScore: Double? = null,
) {

    companion object {
        fun from(applicant: Applicant): ApplicantDto = ApplicantDto(
            id = applicant.id!!,
            name = applicant.name,
            email = applicant.email,
            phoneNumber = applicant.phoneNumber,
            age = applicant.age,
            department = applicant.department,
            studentId = applicant.studentId,
            part = PartDto.from(applicant.part),
            state = applicant.state,
            applicationDateTime = applicant.applicationDateTime,
            applicationSemester = SemesterDto.from(applicant.applicationSemester),
            academicSemester = applicant.academicSemester,
            availableTimes = applicant.availableTimes,
        )
    }
}
