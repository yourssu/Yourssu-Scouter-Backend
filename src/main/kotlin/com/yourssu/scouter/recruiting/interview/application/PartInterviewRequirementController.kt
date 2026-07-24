package com.yourssu.scouter.recruiting.interview.application

import com.yourssu.scouter.recruiting.interview.application.dto.ReadPartInterviewRequirementResponse
import com.yourssu.scouter.recruiting.interview.application.dto.UpdatePartInterviewRequirementRequest
import com.yourssu.scouter.recruiting.interview.business.PartInterviewRequirementService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "면접 요구조건")
@RestController
class PartInterviewRequirementController(
    private val partInterviewRequirementService: PartInterviewRequirementService,
) {

    @Operation(summary = "파트별 면접 요구조건 조회")
    @GetMapping("/parts/{partId}/interviews/requirements")
    fun readByPartId(
        @PathVariable partId: Long,
    ): ResponseEntity<ReadPartInterviewRequirementResponse> {
        return ResponseEntity.ok(
            ReadPartInterviewRequirementResponse.from(partInterviewRequirementService.readByPartId(partId)),
        )
    }

    @Operation(summary = "파트별 면접 요구조건 수정")
    @PutMapping("/parts/{partId}/interviews/requirements")
    fun upsert(
        @PathVariable partId: Long,
        @RequestBody @Valid request: UpdatePartInterviewRequirementRequest,
    ): ResponseEntity<Unit> {
        partInterviewRequirementService.upsert(partId, request.culture!!, request.team!!, request.job!!)

        return ResponseEntity.ok().build()
    }
}
