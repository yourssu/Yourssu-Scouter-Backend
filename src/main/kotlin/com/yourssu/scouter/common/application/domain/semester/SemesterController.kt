package com.yourssu.scouter.common.application.domain.semester

import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.common.business.domain.semester.SemesterService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.net.URI
import java.time.LocalDate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@Tag(name = "학과/학기/단과대")
@RestController
class SemesterController(
    private val semesterService: SemesterService,
) {

    @Operation(summary= "학기 생성", description = "연도, 학기 조합으로 학기를 생성합니다.")
    @PostMapping("/semesters")
    fun create(
        @RequestBody @Valid request: CreateSemesterRequest,
    ): ResponseEntity<Unit> {
        val semesterId: Long = semesterService.create(request.year, request.term)

        return ResponseEntity.created(URI.create("/semesters/$semesterId")).build()
    }

    @Operation(summary = "학기 전체 조회", description = "생성된 전체 학기 정보를 조회합니다.")
    @GetMapping("/semesters")
    fun readAll(): ResponseEntity<List<ReadSemesterResponse>> {
        val semesterDtos: List<SemesterDto> = semesterService.readAllByReverseOrder()
        val response: List<ReadSemesterResponse> = semesterDtos.map { ReadSemesterResponse.from(it) }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "현재 학기 조회", description = "현재 학기에 해당하는 정보를 조회합니다.")
    @GetMapping("/semesters/now")
    fun readByDate(): ResponseEntity<ReadSemesterResponse> {
        val now: LocalDate = LocalDate.now()
        val semesterDto: SemesterDto = semesterService.readByDate(now)
        val response: ReadSemesterResponse = ReadSemesterResponse.from(semesterDto)

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "학기 삭제", description = "특정 학기 정보를 삭제합니다.")
    @DeleteMapping("/semesters/{semesterId}")
    fun deleteById(
        @PathVariable semesterId: Long,
    ): ResponseEntity<Unit> {
        semesterService.deleteById(semesterId)

        return ResponseEntity.noContent().build()
    }
}
