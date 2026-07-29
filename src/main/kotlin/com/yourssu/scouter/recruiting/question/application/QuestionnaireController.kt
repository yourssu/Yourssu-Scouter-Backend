package com.yourssu.scouter.recruiting.question.application

import com.yourssu.scouter.recruiting.question.application.dto.ReadQuestionnaireResponse
import com.yourssu.scouter.recruiting.question.application.dto.SaveQuestionnaireRequest
import com.yourssu.scouter.recruiting.question.business.QuestionnaireService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "면접 질문지")
@RestController
class QuestionnaireController(
    private val questionnaireService: QuestionnaireService,
) {

    @Operation(summary = "지원자별 면접 질문지 조회")
    @GetMapping("/applicants/{applicantId}/interviews/questionnaires")
    fun read(
        @PathVariable applicantId: Long,
    ): ResponseEntity<ReadQuestionnaireResponse> {
        return ResponseEntity.ok(ReadQuestionnaireResponse.from(questionnaireService.readByApplicantId(applicantId)))
    }

    @Operation(summary = "지원자별 면접 질문지 수정", description = "요청 바디의 문항 목록으로 기존 질문지를 전체 치환합니다.")
    @PutMapping("/applicants/{applicantId}/interviews/questionnaires")
    fun upsert(
        @PathVariable applicantId: Long,
        @RequestBody @Valid request: SaveQuestionnaireRequest,
    ): ResponseEntity<ReadQuestionnaireResponse> {
        val result = questionnaireService.upsert(applicantId, request.toCommand())

        return ResponseEntity.ok(ReadQuestionnaireResponse.from(result))
    }
}
