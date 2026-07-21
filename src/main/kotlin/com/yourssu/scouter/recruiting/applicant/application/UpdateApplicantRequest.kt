package com.yourssu.scouter.recruiting.applicant.application

import com.yourssu.scouter.recruiting.applicant.business.UpdateApplicantCommand
import com.yourssu.scouter.recruiting.applicant.implement.ApplicantState
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import java.time.Instant
import java.time.LocalDate

data class UpdateApplicantRequest(

    val partId: Long? = null,

    val name: String? = null,

    @field:Schema(example = "UNDER_REVIEW", description = "UNDER_REVIEW | DOCUMENT_ACCEPTED | DOCUMENT_REJECTED | INTERVIEW_ACCEPTED | INTERVIEW_REJECTED | INCUBATING_REJECTED | FINAL_ACCEPTED")
    val state: String? = null,

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

    val availableTimes: List<Instant>? = null,
) {

    fun toCommand(applicantId: Long): UpdateApplicantCommand = UpdateApplicantCommand(
        targetApplicantId = applicantId,
        partId = partId,
        name = name,
        state = state?.let {
            runCatching { ApplicantState.valueOf(it) }
                .getOrElse { throw IllegalArgumentException("허용되지 않는 state 값입니다: $it") }
        },
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
