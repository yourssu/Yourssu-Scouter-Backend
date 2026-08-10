package com.yourssu.scouter.recruiting.interviewQuestion.application

import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.CreateQuestionsRequest
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.ReadQuestionResponse
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.UpdatePartQuestionsRequest
import com.yourssu.scouter.recruiting.interviewQuestion.business.QuestionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "면접 질문")
@RestController
class QuestionController(private val questionService: QuestionService) {
    @Operation(summary = "파트별 면접 공통 질문 전체 수정", description = "id가 있으면 수정하고 없으면 생성하며, 누락된 기존 질문은 삭제합니다.")
    @PutMapping("/parts/{partId}/interviews/questions")
    fun upsertParts(@PathVariable partId: Long, @RequestBody @Valid request: UpdatePartQuestionsRequest): ResponseEntity<List<ReadQuestionResponse>> =
        ResponseEntity.ok(questionService.upsertParts(partId, request).map(ReadQuestionResponse::from))

    @Operation(summary = "전역·문화 면접 질문 생성", description = "GLOBAL 및 CULTURE 질문을 생성합니다.")
    @PostMapping("/interviews/questions")
    fun create(@RequestBody @Valid request: CreateQuestionsRequest): ResponseEntity<List<ReadQuestionResponse>> =
        ResponseEntity.ok(questionService.create(request).map(ReadQuestionResponse::from))

    @Operation(summary = "파트별 면접 질문 및 요구조건 조회", description = "GLOBAL, CULTURE, PART 질문과 매핑된 요구조건을 조회합니다.")
    @GetMapping("/parts/{partId}/interviews/questions")
    fun readAll(@PathVariable partId: Long): ResponseEntity<List<ReadQuestionResponse>> =
        ResponseEntity.ok(questionService.readAll(partId).map(ReadQuestionResponse::from))
}
