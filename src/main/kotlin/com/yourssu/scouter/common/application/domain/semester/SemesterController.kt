package com.yourssu.scouter.common.application.domain.semester

import com.yourssu.scouter.common.application.support.authentication.AuthUser
import com.yourssu.scouter.common.application.support.authentication.AuthUserInfo
import com.yourssu.scouter.common.business.domain.semester.SemesterDto
import com.yourssu.scouter.common.business.domain.semester.SemesterService
import jakarta.validation.Valid
import java.net.URI
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SemesterController(
    private val semesterService: SemesterService,
) {

    @PostMapping("/semesters")
    fun create(
        @AuthUser authUserInfo: AuthUserInfo,
        @RequestBody @Valid request: CreateSemesterRequest,
    ): ResponseEntity<Unit> {
        val semesterId: Long = semesterService.create(request.year, request.term)

        return ResponseEntity.created(URI.create("/semesters/$semesterId")).build()
    }

    @GetMapping("/semesters")
    fun readAll(
        @AuthUser authUserInfo: AuthUserInfo,
    ): ResponseEntity<List<ReadSemesterResponse>> {
        val semesterDtos: List<SemesterDto> = semesterService.readAll()
        val response: List<ReadSemesterResponse> = semesterDtos.map { ReadSemesterResponse.from(it) }

        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/semesters/{semesterId}")
    fun deleteById(
        @AuthUser authUserInfo: AuthUserInfo,
        @PathVariable semesterId: Long,
    ): ResponseEntity<Unit> {
        semesterService.deleteById(semesterId)

        return ResponseEntity.noContent().build()
    }
}
