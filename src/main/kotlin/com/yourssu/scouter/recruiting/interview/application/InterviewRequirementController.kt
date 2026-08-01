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

    @Operation(summary = "전역 면접 요구조건 조회", description = "특정 파트에 속하지 않고 모든 파트에 공통 적용되는 면접 요구조건을 조회합니다.")
    @GetMapping("/interviews/requirements/global")
    fun readGlobal(
        @RequestParam @Pattern(regexp = "^\\d{4}-[12]$", message = "semester must use YYYY-1 or YYYY-2 format") semester: String,
    ): ResponseEntity<ReadInterviewRequirementResponse> {
        val dto = partInterviewRequirementService.readGlobalBySemester(Semester.of(semester))
        return ResponseEntity.ok(ReadInterviewRequirementResponse.from(dto))
    }

    @Operation(
        summary = "전역 면접 요구조건 수정",
        description = "모든 파트에 공통 적용되는 면접 요구조건을 수정합니다. 잠긴(평가가 진행 중이거나 완료된) 루브릭을 가진 파트가 하나라도 있으면 수정할 수 없습니다.",
    )
    @PutMapping("/interviews/requirements/global")
    fun upsertGlobal(
        @RequestParam @Pattern(regexp = "^\\d{4}-[12]$", message = "semester must use YYYY-1 or YYYY-2 format") semester: String,
        @RequestBody @Valid request: UpdateInterviewRequirementRequest,
    ): ResponseEntity<Unit> {
        partInterviewRequirementService.saveAllGlobal(Semester.of(semester), request)
        return ResponseEntity.ok().build()
    }
}
