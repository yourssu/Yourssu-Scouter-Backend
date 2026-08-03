package com.yourssu.scouter.recruiting.applicant.business.dto

import com.yourssu.scouter.recruiting.applicant.implement.AssignmentResult

data class UpdateAssignmentResultCommand(
    val applicantId: Long,
    val assignmentResult: AssignmentResult,
)
