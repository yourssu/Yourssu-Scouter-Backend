package com.yourssu.scouter.ats.business.domain.applicant

data class ApplicantSyncResult(
    val successMessages: List<String> = emptyList(),
    val failureMessages: List<String> = emptyList(),
) {
    companion object {
        fun success(successMessage: String): ApplicantSyncResult {
            return ApplicantSyncResult(successMessages = listOf(successMessage))
        }

        fun failure(failureMessage: String): ApplicantSyncResult {
            return ApplicantSyncResult(failureMessages = listOf(failureMessage))
        }
    }
}
