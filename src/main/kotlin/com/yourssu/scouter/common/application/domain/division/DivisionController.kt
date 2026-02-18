package com.yourssu.scouter.common.application.domain.division

import com.yourssu.scouter.common.business.domain.division.DivisionService
import com.yourssu.scouter.common.business.domain.division.ReadDivisionsResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "구분/파트")
@RestController
class DivisionController(
    private val divisionService: DivisionService,
) {

    @Operation(summary = "멤버 구분 목록 조회", description = "운영, 개발, 디자인 등 각 구분의 목록을 조회합니다.")
    @GetMapping("/divisions")
    fun readAll(): ResponseEntity<List<ReadDivisionResponse>> {
        val result: ReadDivisionsResult = divisionService.readAll()
        val response: List<ReadDivisionResponse> = result.divisionDtos.map { ReadDivisionResponse.from(it) }

        return ResponseEntity.ok(response)
    }
}
