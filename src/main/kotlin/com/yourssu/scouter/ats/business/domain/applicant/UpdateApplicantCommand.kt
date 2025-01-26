package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.ats.implement.domain.applicant.ApplicantState
import java.time.LocalDate

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
    val applicantSemesterId: Long? = null,
)
