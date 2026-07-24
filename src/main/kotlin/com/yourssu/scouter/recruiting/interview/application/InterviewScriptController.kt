package com.yourssu.scouter.recruiting.interview.application

import com.yourssu.scouter.recruiting.interview.application.dto.ReadInterviewScriptResponse
import com.yourssu.scouter.recruiting.interview.application.dto.UpdateInterviewScriptRequest
import com.yourssu.scouter.recruiting.interview.business.InterviewScriptService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "면접 스크립트")
@RestController
class InterviewScriptController(
    private val interviewScriptService: InterviewScriptService,
) {

    @Operation(summary = "파트별 면접 스크립트 조회")
    @GetMapping("/parts/{partId}/interviews/scripts")
    fun readByPartId(
        @PathVariable partId: Long,
    ): ResponseEntity<ReadInterviewScriptResponse> {
        return ResponseEntity.ok(ReadInterviewScriptResponse.from(interviewScriptService.readByPartId(partId)))
    }

    @Operation(summary = "파트별 면접 스크립트 수정")
    @PutMapping("/parts/{partId}/interviews/scripts")
    fun upsert(
        @PathVariable partId: Long,
        @RequestBody @Valid request: UpdateInterviewScriptRequest,
    ): ResponseEntity<Unit> {
        interviewScriptService.upsert(partId, request.opening!!, request.closing!!)

        return ResponseEntity.ok().build()
    }
}
