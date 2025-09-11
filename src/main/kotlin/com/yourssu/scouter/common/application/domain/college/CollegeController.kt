package com.yourssu.scouter.common.application.domain.college

import com.yourssu.scouter.common.business.domain.college.CollegeService
import com.yourssu.scouter.common.business.domain.college.ReadCollegesResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "학과/학기/단과대")
@RestController
class CollegeController(
    private val collegeService: CollegeService,
) {

    @Operation(summary = "전체 단과대 조회", description = "전체 단과대의 정보를 조회합니다.")
    @GetMapping("/colleges")
    fun readAll(): ResponseEntity<List<ReadCollegeResponse>> {
        val result: ReadCollegesResult = collegeService.readAll()
        val response: List<ReadCollegeResponse> = result.collegeDtos.map { ReadCollegeResponse.from(it) }

        return ResponseEntity.ok(response)
    }
}
