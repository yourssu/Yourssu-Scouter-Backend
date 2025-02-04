package com.yourssu.scouter.ats.application.domain.applicant

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.ats.business.support.utils.ApplicantStateConverter
import com.yourssu.scouter.ats.business.domain.applicant.UpdateApplicantCommand
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class UpdateApplicantRequest(

    val partId: Long? = null,

    val name: String? = null,

    val state: String? = null,

    @field:JsonFormat(pattern = "yyyy.MM.dd")
    val applicationDate: LocalDate? = null,

    val email: String? = null,

    @field:Pattern(
        regexp = "^010-\\d{4}-\\d{4}\$",
        message = "전화번호는 \\{ 010-xxxx-xxxx \\} 형식이어야 합니다"
    )
    val phoneNumber: String? = null,

    val departmentId: Long? = null,

    val studentId: String? = null,

    val semesterId: Long? = null,

    val age: String? = null,

    @field:Pattern(
        regexp = "^\\d-\\d\$",
        message = "재학 학기는 \\{ 학년-학기 \\} 형식이어야 합니다"
    )
    val academicSemester: String? = null,
) {

    fun toCommand(applicantId: Long): UpdateApplicantCommand = UpdateApplicantCommand(
        targetApplicantId = applicantId,
        partId = partId,
        name = name,
        state = state?.let { ApplicantStateConverter.convertToEnum(it) },
        applicationDate = applicationDate,
        email = email,
        phoneNumber = phoneNumber,
        departmentId = departmentId,
        studentId = studentId,
        applicantSemesterId = semesterId,
        age = age,
        academicSemester = academicSemester,
    )
}
