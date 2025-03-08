package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantSyncResult
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantSyncService
import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicantSyncController(
    private val applicantSyncService: ApplicantSyncService,
) {

    @PostMapping("/applicants/include-from-forms")
    fun includeFromForms(
        @AuthUser authUserInfo: AuthUserInfo,
    ): ResponseEntity<ApplicantSyncResponse> {
        val result: ApplicantSyncResult = applicantSyncService.includeFromForms(authUserInfo.userId)
        val response: ApplicantSyncResponse = ApplicantSyncResponse.from(result)

        return ResponseEntity.ok(response)
    }

    @PostMapping("/applicants/include-from-forms/{semesterString}")
    fun includeFromForms(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable semesterString: String,
    ): ResponseEntity<ApplicantSyncResponse> {
        val result: ApplicantSyncResult = applicantSyncService.includeFromForms(authUserInfo.userId, semesterString)
        val response: ApplicantSyncResponse = ApplicantSyncResponse.from(result)

        return ResponseEntity.ok(response)
    }
}

