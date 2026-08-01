package com.yourssu.scouter.recruiting.interview.application

import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.interview.application.dto.ReadInterviewRequirementResponse
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewRequirementRequest
import com.yourssu.scouter.recruiting.interview.business.InterviewRequirementService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

@Tag(name = "면접 요구조건")
@RestController
@Validated
class InterviewRequirementController(
    private val partInterviewRequirementService: InterviewRequirementService,
) {

    @Operation(summary = "파트별 면접 요구조건 조회")
    @GetMapping("/parts/{partId}/interviews/requirements")
    fun readByPartId(
        @PathVariable partId: Long,
        @RequestParam @Pattern(regexp = "^\\d{4}-[12]$", message = "semester must use YYYY-1 or YYYY-2 format") semester: String,
    ): ResponseEntity<ReadInterviewRequirementResponse> {
        val dto = partInterviewRequirementService.readByPartIdAndSemester(partId, Semester.of(semester))
        return ResponseEntity.ok(ReadInterviewRequirementResponse.from(dto))
    }

    @Operation(summary = "파트별 면접 요구조건 수정")
    @PutMapping("/parts/{partId}/interviews/requirements")
    fun upsert(
        @PathVariable partId: Long,
        @RequestParam @Pattern(regexp = "^\\d{4}-[12]$", message = "semester must use YYYY-1 or YYYY-2 format") semester: String,
        @RequestBody @Valid request: UpdateInterviewRequirementRequest,
    ): ResponseEntity<Unit> {
        partInterviewRequirementService.saveAll(partId, Semester.of(semester), request)
        return ResponseEntity.ok().build()
    }
}
