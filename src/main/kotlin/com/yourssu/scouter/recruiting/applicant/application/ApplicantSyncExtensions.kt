package com.yourssu.scouter.recruiting.applicant.application

import com.yourssu.scouter.recruiting.applicant.application.dto.ApplicantSyncResponse

import com.yourssu.scouter.recruiting.applicant.business.dto.ApplicantSyncResult
import java.net.URI
import org.springframework.http.ResponseEntity

fun ApplicantSyncResult.toCreatedResponse(location: URI, mapper: (ApplicantSyncResult) -> ApplicantSyncResponse): ResponseEntity<ApplicantSyncResponse> {
    val response: ApplicantSyncResponse = mapper(this)
    return ResponseEntity.created(location).body(response)
}
