package com.yourssu.scouter.recruiting.interview.application

import com.yourssu.scouter.recruiting.interview.application.dto.ReadInterviewMemoResponse
import com.yourssu.scouter.recruiting.interview.application.dto.SaveInterviewMemoRequest
import com.yourssu.scouter.recruiting.interview.business.InterviewMemoService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "면접 메모")
@RestController
class InterviewMemoController(
    private val interviewMemoService: InterviewMemoService,
) {

    @Operation(summary = "지원자별 질문 메모 목록 조회")
    @GetMapping("/applicants/{applicantId}/interviews/memos")
    fun read(
        @PathVariable applicantId: Long,
    ): ResponseEntity<List<ReadInterviewMemoResponse>> {
        val memos = interviewMemoService.readByApplicantId(applicantId)

        return ResponseEntity.ok(memos.map(ReadInterviewMemoResponse::from))
    }

    @Operation(summary = "지원자별 질문 메모 일괄 수정", description = "요청 바디의 메모 목록으로 기존 메모를 전체 치환합니다.")
    @PutMapping("/applicants/{applicantId}/interviews/memos")
    fun upsert(
        @PathVariable applicantId: Long,
        @RequestBody @Valid requests: List<SaveInterviewMemoRequest>,
    ): ResponseEntity<List<ReadInterviewMemoResponse>> {
        val result = interviewMemoService.upsert(applicantId, requests.map { it.toCommand() })

        return ResponseEntity.ok(result.map(ReadInterviewMemoResponse::from))
    }
}
