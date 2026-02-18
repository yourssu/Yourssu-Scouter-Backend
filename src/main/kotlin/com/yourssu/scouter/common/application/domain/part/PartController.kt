package com.yourssu.scouter.common.application.domain.part

import com.yourssu.scouter.common.business.domain.part.PartService
import com.yourssu.scouter.common.business.domain.part.ReadPartsResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "구분/파트")
@RestController
class PartController(
    private val partService: PartService,
) {

    @Operation(summary = "파트(팀) 목록 조회", description = "Head lead, finance 등 파트 목록을 조회합니다.")
    @GetMapping("/parts")
    fun readAll(): ResponseEntity<List<ReadPartsResponse>> {
        val result: ReadPartsResult = partService.readAll()
        val response: List<ReadPartsResponse> = result.partDtos.map { ReadPartsResponse.from(it) }

        return ResponseEntity.ok(response)
    }
}
