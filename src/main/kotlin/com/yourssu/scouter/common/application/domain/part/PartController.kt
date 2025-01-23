package com.yourssu.scouter.common.application.domain.part

import com.yourssu.scouter.common.business.domain.part.PartService
import com.yourssu.scouter.common.business.domain.part.ReadPartsResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class PartController(
    private val partService: PartService,
) {

    @GetMapping("/parts")
    fun readAll(): ResponseEntity<List<ReadPartsResponse>> {
        val result: ReadPartsResult = partService.readAll()
        val response: List<ReadPartsResponse> = result.partDtos.map { ReadPartsResponse.from(it) }

        return ResponseEntity.ok(response)
    }
}
