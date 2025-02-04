package com.yourssu.scouter.ats.application.domain.applicant

import com.yourssu.scouter.ats.business.domain.applicant.ApplicantDto
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantService
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantSyncResult
import com.yourssu.scouter.ats.business.domain.applicant.ApplicantSyncService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class ApplicantController(
    private val applicantService: ApplicantService,
    private val applicantSyncService: ApplicantSyncService,
) {

    @PostMapping("/applicants")
    fun create(
        @RequestBody @Valid request: CreateApplicantRequest,
    ): ResponseEntity<Unit> {
        val command = request.toCommand()
        val applicantId: Long = applicantService.create(command)

        return ResponseEntity.created(URI.create("/applicants/$applicantId")).build()
    }

    @PostMapping("/applicants/include-from-forms")
    fun includeFromForms(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
    ): ResponseEntity<ApplicantSyncResult> {
        val authUserId = authorization.toLong() // TODO: 임시로 사용자 ID를 Authorization 헤더에서 추출하는 방식으로 구현

        val result: ApplicantSyncResult = applicantSyncService.includeFromForms(authUserId)

        return ResponseEntity.ok(result)
    }

    @PostMapping("/applicants/include-from-forms/{semesterString}")
    fun includeFromForms(
        @RequestHeader(HttpHeaders.AUTHORIZATION) authorization: String,
        @PathVariable semesterString: String,
    ): ResponseEntity<ApplicantSyncResult> {
        val authUserId = authorization.toLong() // TODO: 임시로 사용자 ID를 Authorization 헤더에서 추출하는 방식으로 구현

        val result: ApplicantSyncResult = applicantSyncService.includeFromForms(authUserId, semesterString)

        return ResponseEntity.ok(result)
    }

    @GetMapping("/applicants")
    fun readAll(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) state: String?,
        @RequestParam(required = false) semesterId: Long?,
    ): ResponseEntity<List<ReadApplicantResponse>> {
        val applicantDtos: List<ApplicantDto> = when {
            !name.isNullOrEmpty() && state.isNullOrEmpty() && semesterId == null -> applicantService.searchByName(name)
            name.isNullOrEmpty() && !state.isNullOrEmpty() && semesterId == null -> applicantService.filterByState(state)
            name.isNullOrEmpty() && state.isNullOrEmpty() && semesterId != null -> applicantService.filterBySemester(semesterId)

            else -> applicantService.readAll()
        }
        val responses: List<ReadApplicantResponse> = applicantDtos.map { ReadApplicantResponse.from(it) }

        return ResponseEntity.ok(responses)
    }

    @PatchMapping("/applicants/{applicantId}")
    fun updateById(
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: UpdateApplicantRequest,
    ): ResponseEntity<Unit> {
        val command = request.toCommand(applicantId)
        applicantService.updateById(command)

        return ResponseEntity.ok().build()
    }

    @GetMapping("/applicants/{applicantId}")
    fun readById(
        @PathVariable applicantId: Long,
    ): ResponseEntity<ReadApplicantResponse> {
        val applicantDto: ApplicantDto = applicantService.readById(applicantId)
        val response = ReadApplicantResponse.from(applicantDto)

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/applicants/{applicantId}")
    fun deleteById(
        @PathVariable applicantId: Long,
    ): ResponseEntity<Unit> {
        applicantService.deleteById(applicantId)

        return ResponseEntity.noContent().build()
    }

    @GetMapping("/applicants/states")
    fun readAllMemberStates(): ResponseEntity<List<String>> {
        val states: List<String> = applicantService.readAllStates()

        return ResponseEntity.ok(states)
    }
}
