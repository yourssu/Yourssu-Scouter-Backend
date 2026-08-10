package com.yourssu.scouter.recruiting.interviewQuestion.application

import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.ReadQuestionResponse
import com.yourssu.scouter.recruiting.interviewQuestion.business.QuestionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import jakarta.validation.Valid
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.CreateQuestionsRequest
import com.yourssu.scouter.recruiting.interviewQuestion.application.dto.UpdatePartQuestionsRequest

@Tag(name = "면접 질문")
@RestController
class QuestionController(
    private val questionService: QuestionService,
) {

    @org.springframework.web.bind.annotation.PutMapping("/parts/{partId}/interviews/questions")
    fun upsertParts(
        @PathVariable partId: Long,
        @RequestBody @Valid request: UpdatePartQuestionsRequest,
    ): ResponseEntity<List<ReadQuestionResponse>> =
        ResponseEntity.ok(questionService.upsertParts(partId, request).map(ReadQuestionResponse::from))

    @PostMapping("/interviews/questions")
    fun create(@RequestBody @Valid request: CreateQuestionsRequest): ResponseEntity<List<ReadQuestionResponse>> {
        return ResponseEntity.ok(questionService.create(request).map(ReadQuestionResponse::from))
    }

    @Operation(summary = "파트별 면접 질문 카탈로그 조회")
    @GetMapping("/parts/{partId}/interviews/questions")
    fun readAll(
        @PathVariable partId: Long,
    ): ResponseEntity<List<ReadQuestionResponse>> {
        val questions = questionService.readAll(partId)

        return ResponseEntity.ok(questions.map(ReadQuestionResponse::from))
    }
}
