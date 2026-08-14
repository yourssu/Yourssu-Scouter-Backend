package com.yourssu.scouter.masterdata.part.application

import com.yourssu.scouter.masterdata.part.application.dto.ReadPartsResponse
import com.yourssu.scouter.masterdata.part.business.PartService
import com.yourssu.scouter.masterdata.part.business.dto.ReadPartsResult
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

    @Operation(summary = "파트(팀) 목록 조회", description = "Head lead, Finance 등 파트 목록을 조회합니다. 파트별 세부 정보(과제 유무 등)도 함께 제공됩니다.")
    @GetMapping("/parts")
    fun readAll(): ResponseEntity<List<ReadPartsResponse>> {
        val result: ReadPartsResult = partService.readAll()
        val response: List<ReadPartsResponse> = result.partDtos.map { ReadPartsResponse.from(it) }
        return ResponseEntity.ok(response)
    }

}
