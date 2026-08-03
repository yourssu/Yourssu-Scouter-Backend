package com.yourssu.scouter.recruiting.applicant.application.dto

import com.yourssu.scouter.recruiting.applicant.business.dto.UpdateAssignmentResultCommand
import com.yourssu.scouter.recruiting.applicant.implement.AssignmentResult

data class UpdateAssignmentResultRequest(
    val assignmentResult: AssignmentResult,
) {
    fun toCommand(applicantId: Long) = UpdateAssignmentResultCommand(
        applicantId = applicantId,
        assignmentResult = assignmentResult,
    )
}
