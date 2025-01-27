package com.yourssu.scouter.common.application.domain.semester

import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.common.business.domain.semester.SemesterService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class SemesterController(
    private val semesterService: SemesterService,
) {

    @GetMapping("/semesters")
    fun readAll(): ResponseEntity<List<ReadSemesterResponse>> {
        val semesterDtos: List<SemesterDto> = semesterService.readAll()
        val response: List<ReadSemesterResponse> = semesterDtos.map { ReadSemesterResponse.from(it) }

        return ResponseEntity.ok(response)
    }
}
