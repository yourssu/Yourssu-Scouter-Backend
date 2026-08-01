package com.yourssu.scouter.recruiting.interviewQuestion.application

import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.ReadAssignedQuestionsResponse
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.SaveAssignedQuestionsRequest
import com.yourssu.scouter.recruiting.interviewQuestion.business.AssignedQuestionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "면접 질문")
@RestController
class AssignedQuestionController(
    private val assignedQuestionService: AssignedQuestionService,
) {

    @Operation(summary = "지원자별 면접 질문 조회")
    @GetMapping("/applicants/{applicantId}/interviews/questions")
    fun read(
        @PathVariable applicantId: Long,
    ): ResponseEntity<ReadAssignedQuestionsResponse> {
        return ResponseEntity.ok(ReadAssignedQuestionsResponse.from(assignedQuestionService.readByApplicantId(applicantId)))
    }

    @Operation(summary = "지원자별 면접 질문 수정", description = "요청 바디의 문항 목록으로 기존 지원자별 질문을 전체 치환합니다.")
    @PutMapping("/applicants/{applicantId}/interviews/questions")
    fun upsert(
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: SaveAssignedQuestionsRequest,
    ): ResponseEntity<ReadAssignedQuestionsResponse> {
        val result = assignedQuestionService.upsert(applicantId, request.toCommand())

        return ResponseEntity.ok(ReadAssignedQuestionsResponse.from(result))
    }
}
