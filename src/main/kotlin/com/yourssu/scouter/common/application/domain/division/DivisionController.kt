package com.yourssu.scouter.common.application.domain.division

import com.yourssu.scouter.common.business.domain.division.DivisionService
import com.yourssu.scouter.common.business.domain.division.ReadDivisionsResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DivisionController(
    private val divisionService: DivisionService,
) {

    @GetMapping("/divisions")
    fun readAll(): ResponseEntity<List<ReadDivisionResponse>> {
        val result: ReadDivisionsResult = divisionService.readAll()
        val response: List<ReadDivisionResponse> = result.divisionDtos.map { ReadDivisionResponse.from(it) }

        return ResponseEntity.ok(response)
    }
}
