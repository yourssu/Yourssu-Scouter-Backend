package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.CreateApplicantCommand
import com.yourssu.scouter.ats.business.support.utils.ApplicantStateConverter
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.Instant
import java.time.LocalDate

data class CreateApplicantRequest(

    @field:NotNull(message = "파트를 입력하지 않았습니다.")
    val partId: Long,

    @field:NotBlank(message = "이름을 입력하지 않았습니다.")
    val name: String,

    @field:NotBlank(message = "상태를 입력하지 않았습니다.")
    @field:Schema(example = "심사 진행 중", description = "심사 진행 중 | 서류 불합 | 면접 불합 | 인큐베이팅 불합 | 최종 합격")
    val state: String,

    @field:NotNull(message = "지원일을 입력하지 않았습니다.")
    @field:Schema(pattern = "yyyy-MM-dd", example = "2025-11-10")
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

    @field:NotNull(message = "면접 가능 시간을 입력하지 않았습니다.")
    val availableTimes: List<Instant>,
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
        applicationSemesterId = semesterId,
        age = age,
        academicSemester = academicSemester,
        availableTimes = availableTimes
    )
}
