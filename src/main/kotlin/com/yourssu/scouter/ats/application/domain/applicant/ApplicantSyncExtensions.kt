package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantSyncResult
import java.net.URI
import org.springframework.http.ResponseEntity

fun ApplicantSyncResult.toCreatedResponse(location: URI, mapper: (ApplicantSyncResult) -> ApplicantSyncResponse): ResponseEntity<ApplicantSyncResponse> {
    val response: ApplicantSyncResponse = mapper(this)
    return ResponseEntity.created(location).body(response)
}
