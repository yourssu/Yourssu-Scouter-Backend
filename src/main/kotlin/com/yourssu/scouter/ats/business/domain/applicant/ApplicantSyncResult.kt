package com.yourssu.scouter.ats.business.domain.applicant

import com.yourssu.scouter.common.implement.support.google.GoogleDriveFile

data class ApplicantSyncResult(
    val successes: List<FormDto>,
    val failures: List<FormDto>,
)

data class FormDto(
    val id: String,
    val name: String,
    val webViewLink: String,
) {

    companion object {
        fun from(form: GoogleDriveFile): FormDto = FormDto(
            id = form.id,
            name = form.name,
            webViewLink = form.webViewLink,
        )
    }
}
