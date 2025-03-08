package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantSyncResult

data class ApplicantSyncResponse(
    val successes: List<String>,
    val failures: List<String>,
) {

    companion object {
        fun from(result: ApplicantSyncResult) = ApplicantSyncResponse(
            successes = result.successeMessages,
            failures = result.failureMessages,
        )
    }
}
