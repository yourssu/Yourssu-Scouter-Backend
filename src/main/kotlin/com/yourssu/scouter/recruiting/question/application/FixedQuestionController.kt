package com.yourssu.scouter.recruiting.question.application

import com.yourssu.scouter.recruiting.question.application.dto.ReadFixedQuestionResponse
import com.yourssu.scouter.recruiting.question.business.FixedQuestionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Tag(name = "고정 질문")
@RestController
class FixedQuestionController(
    private val fixedQuestionService: FixedQuestionService,
) {

    @Operation(summary = "고정 질문 전역 풀 조회")
    @GetMapping("/parts/{partId}/interviews/questions/fixed")
    fun readAll(
        @PathVariable partId: Long,
    ): ResponseEntity<List<ReadFixedQuestionResponse>> {
        val questions = fixedQuestionService.readAll(partId)

        return ResponseEntity.ok(questions.map(ReadFixedQuestionResponse::from))
    }
}
