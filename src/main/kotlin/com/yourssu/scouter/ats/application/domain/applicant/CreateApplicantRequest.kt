package com.yourssu.scouter.ats.application.domain.applicant

import com.fasterxml.jackson.annotation.JsonFormat
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantStateConverter
import com.yourssu.scouter.ats.business.domain.applicant.CreateApplicantCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDate

data class CreateApplicantRequest(

    @NotNull(message = "파트를 입력하지 않았습니다.")
    val partId: Long,

    @NotBlank(message = "이름을 입력하지 않았습니다.")
    val name: String,

    @NotBlank(message = "상태를 입력하지 않았습니다.")
    val state: String,

    @NotNull(message = "지원일을 입력하지 않았습니다.")
    @JsonFormat(pattern = "yyyy.MM.dd")
    val applicationDate: LocalDate,

    @Email(message = "이메일 형식이 아닙니다.")
    val email: String,

    @NotBlank(message = "전화번호를 입력하지 않았습니다.")
    @Pattern(
        regexp = "^010-\\d{4}-\\d{4}\$",
        message = "전화번호는 \\{ 010-xxxx-xxxx \\} 형식이어야 합니다"
    )
    val phoneNumber: String,

    @NotNull(message = "학과를 입력하지 않았습니다.")
    val departmentId: Long,

    @NotBlank(message = "학번을 입력하지 않았습니다.")
    val studentId: String,

    @NotNull(message = "학기를 입력하지 않았습니다.")
    val semesterId: Long,

    @NotBlank(message = "나이를 입력하지 않았습니다.")
    val age: String,
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
    )
}
