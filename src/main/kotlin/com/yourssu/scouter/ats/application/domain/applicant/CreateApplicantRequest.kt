package com.yourssu.scouter.ats.application.domain.applicant

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.ats.business.domain.applicant.CreateApplicantCommand
import com.yourssu.scouter.ats.business.support.utils.ApplicantStateConverter
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class CreateApplicantRequest(

    @field:NotNull(message = "파트를 입력하지 않았습니다.")
    val partId: Long,

    @field:NotBlank(message = "이름을 입력하지 않았습니다.")
    val name: String,

    @field:NotBlank(message = "상태를 입력하지 않았습니다.")
    val state: String,

    @field:NotNull(message = "지원일을 입력하지 않았습니다.")
    @field:JsonFormat(pattern = "yyyy.MM.dd")
    val applicationDate: LocalDate,

    @field:Email(message = "이메일 형식이 아닙니다.")
    val email: String,

    @field:NotBlank(message = "전화번호를 입력하지 않았습니다.")
    @field:Pattern(
        regexp = "^010-\\d{4}-\\d{4}\$",
        message = "전화번호는 \\{ 010-xxxx-xxxx \\} 형식이어야 합니다"
    )
    val phoneNumber: String,

    @field:NotNull(message = "학과를 입력하지 않았습니다.")
    val departmentId: Long,

    @field:NotBlank(message = "학번을 입력하지 않았습니다.")
    val studentId: String,

    @field:NotNull(message = "학기를 입력하지 않았습니다.")
    val semesterId: Long,

    @field:NotBlank(message = "나이를 입력하지 않았습니다.")
    val age: String,

    @field:NotBlank(message = "재학 학기를 입력하지 않았습니다.")
    @field:Pattern(
        regexp = "^\\d-\\d\$",
        message = "재학 학기는 \\{ 학년-학기 \\} 형식이어야 합니다"
    )
    val academicSemester: String,
) {

    fun toCommand(): CreateApplicantCommand = CreateApplicantCommand(
        partId = partId,
        name = name,
        state = ApplicantStateConverter.convertToEnum(state),
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
