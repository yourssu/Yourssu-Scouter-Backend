package com.yourssu.scouter.recruiting.applicant.application.dto

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantSyncResult

data class ApplicantSyncResponse(
    val successes: List<String>,
    val failures: List<String>,
) {

    companion object {
        fun from(result: ApplicantSyncResult) = ApplicantSyncResponse(
            successes = result.successMessages,
            failures = result.failureMessages,
        )
    }
}
