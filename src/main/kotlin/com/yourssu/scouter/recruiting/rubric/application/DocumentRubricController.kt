package com.yourssu.scouter.recruiting.rubric.application

import com.yourssu.scouter.recruiting.rubric.business.DocumentSectionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "서류 평가 문항")
@RestController
class DocumentRubricController(
    private val documentSectionService: DocumentSectionService,
) {

    @Operation(summary = "서류 평가 문항 조회")
    @GetMapping("/parts/{partId}/documents/rubrics")
    fun readByPartId(
        @PathVariable partId: Long,
    ): ResponseEntity<List<ReadRubricResponse>> {
        val sections = documentSectionService.readByPartId(partId)

        return ResponseEntity.ok(sections.map(ReadRubricResponse::from))
    }

    @Operation(summary = "서류 평가 문항 수정", description = "배점과 평가 지표만 수정합니다. 배점 합계는 100이어야 합니다.")
    @PutMapping("/parts/{partId}/documents/rubrics")
    fun updateRubrics(
        @PathVariable partId: Long,
        @RequestBody @Valid requests: List<UpdateRubricRequest>,
    ): ResponseEntity<Unit> {
        documentSectionService.updateRubrics(partId, requests.map { it.toCommand() })

        return ResponseEntity.ok().build()
    }
}
