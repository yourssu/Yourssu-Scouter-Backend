package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantDto
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicantController(
    private val applicantService: ApplicantService,
) {

    @GetMapping("/applicants")
    fun readAll(): ResponseEntity<List<ReadApplicantResponse>> {
        val applicantDtos: List<ApplicantDto> = applicantService.readAll()
        val responses: List<ReadApplicantResponse> = applicantDtos.map { ReadApplicantResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @GetMapping("/applicants/{applicantId}")
    fun readById(
        @PathVariable applicantId: Long,
    ): ResponseEntity<ReadApplicantResponse> {
        val applicantDto: ApplicantDto = applicantService.readById(applicantId)
        val response = ReadApplicantResponse.from(applicantDto)

        return ResponseEntity.ok(response)
    }
}
