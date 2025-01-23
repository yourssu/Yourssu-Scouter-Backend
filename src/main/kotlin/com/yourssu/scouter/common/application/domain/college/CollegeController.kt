package com.yourssu.scouter.common.application.domain.college

import com.yourssu.scouter.common.business.domain.college.CollegeService
import com.yourssu.scouter.common.business.domain.college.ReadCollegesResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CollegeController(
    private val collegeService: CollegeService,
) {

    @GetMapping("/colleges")
    fun readAll(): ResponseEntity<List<ReadCollegeResponse>> {
        val result: ReadCollegesResult = collegeService.readAll()
        val response: List<ReadCollegeResponse> = result.collegeDtos.map { ReadCollegeResponse.from(it) }

        return ResponseEntity.ok(response)
    }
}
