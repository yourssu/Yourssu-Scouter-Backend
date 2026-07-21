package com.yourssu.scouter.recruiting.deadline.application

import com.yourssu.scouter.recruiting.deadline.business.PartDocumentDeadlineService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "서류 평가 마감일")
@RestController
class PartDocumentDeadlineController(
    private val partDocumentDeadlineService: PartDocumentDeadlineService,
) {

    @Operation(summary = "마감일 조회")
    @GetMapping("/parts/{partId}/documents/deadline")
    fun readByPartId(
        @PathVariable partId: Long,
    ): ResponseEntity<ReadDeadlineResponse> {
        return ResponseEntity.ok(ReadDeadlineResponse.from(partDocumentDeadlineService.readByPartId(partId)))
    }

    @Operation(summary = "마감일 설정")
    @PutMapping("/parts/{partId}/documents/deadline")
    fun upsert(
        @PathVariable partId: Long,
        @RequestBody @Valid request: UpdateDeadlineRequest,
    ): ResponseEntity<Unit> {
        partDocumentDeadlineService.upsert(partId, request.deadline!!)

        return ResponseEntity.ok().build()
    }
}
