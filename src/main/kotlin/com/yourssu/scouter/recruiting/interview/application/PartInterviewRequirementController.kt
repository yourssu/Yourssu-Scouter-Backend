package com.yourssu.scouter.recruiting.interview.application

import com.yourssu.scouter.common.semester.implement.Semester
import com.yourssu.scouter.recruiting.interview.application.dto.ReadPartInterviewRequirementResponse
import com.yourssu.scouter.recruiting.interview.application.dto.UpdatePartInterviewRequirementRequest
import com.yourssu.scouter.recruiting.interview.business.PartInterviewRequirementService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "면접 요구조건")
@RestController
class PartInterviewRequirementController(
    private val partInterviewRequirementService: PartInterviewRequirementService,
) {

    @Operation(summary = "파트별 면접 요구조건 조회")
    @GetMapping("/parts/{partId}/interviews/requirements")
    fun readByPartId(
        @PathVariable partId: Long,
        @RequestParam semester: String,
    ): ResponseEntity<ReadPartInterviewRequirementResponse> {
        val dto = partInterviewRequirementService.readByPartIdAndSemester(partId, Semester.of(semester))
        return ResponseEntity.ok(ReadPartInterviewRequirementResponse.from(dto))
    }

    @Operation(summary = "파트별 면접 요구조건 수정")
    @PutMapping("/parts/{partId}/interviews/requirements")
    fun upsert(
        @PathVariable partId: Long,
        @RequestParam semester: String,
        @RequestBody @Valid request: UpdatePartInterviewRequirementRequest,
    ): ResponseEntity<Unit> {
        partInterviewRequirementService.saveAll(partId, Semester.of(semester), request)
        return ResponseEntity.ok().build()
    }
}
