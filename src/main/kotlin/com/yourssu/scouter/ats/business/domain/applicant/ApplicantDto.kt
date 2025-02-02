package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.Applicant
import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import com.yourssu.scouter.common.business.domain.department.DepartmentDto
import com.yourssu.scouter.common.business.domain.part.PartDto
import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import java.time.LocalDate

data class ApplicantDto(
    val id: Long,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val age: String,
    val department: DepartmentDto,
    val studentId: String,
    val part: PartDto,
    val state: ApplicantState,
    val applicationDate: LocalDate,
    val applicationSemester: SemesterDto,
    val academicSemester: String,
) {

    companion object {
        fun from(applicant: Applicant): ApplicantDto = ApplicantDto(
            id = applicant.id!!,
            name = applicant.name,
            email = applicant.email,
            phoneNumber = applicant.phoneNumber,
            age = applicant.age,
            department = DepartmentDto.from(applicant.department),
            studentId = applicant.studentId,
            part = PartDto.from(applicant.part),
            state = applicant.state,
            applicationDate = applicant.applicationDate,
            applicationSemester = SemesterDto.from(applicant.applicationSemester),
            academicSemester = applicant.academicSemester,
        )
    }
}
