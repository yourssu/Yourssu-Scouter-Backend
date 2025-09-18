package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import java.time.LocalDate
import java.time.LocalDateTime

data class UpdateApplicantCommand(
    val targetApplicantId: Long,
    val name: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val age: String? = null,
    val departmentId: Long? = null,
    val studentId: String? = null,
    val partId: Long? = null,
    val state: ApplicantState? = null,
    val applicationDate: LocalDate? = null,
    val applicationSemesterId: Long? = null,
    val academicSemester: String? = null,
    val availableTimes: List<LocalDateTime>? = null,
)
