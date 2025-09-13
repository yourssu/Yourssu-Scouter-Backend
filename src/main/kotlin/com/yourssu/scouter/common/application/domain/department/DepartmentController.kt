package com.yourssu.scouter.common.application.domain.department

import com.yourssu.scouter.common.business.domain.department.DepartmentService
import com.yourssu.scouter.common.business.domain.department.ReadDepartmentsResult
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@Tag(name = "학과/학기/단과대")
@RestController
class DepartmentController(
    private val departmentService: DepartmentService,
) {

    @Operation(summary = "전체 학과 조회", description = "전체 학과 정보를 조회합니다.")
    @GetMapping("/departments")
    fun readAll(): ResponseEntity<List<ReadDepartmentsResponse>> {
        val result: ReadDepartmentsResult = departmentService.readAll()
        val response: List<ReadDepartmentsResponse> = result.departmentDtos.map { ReadDepartmentsResponse.from(it) }

        return ResponseEntity.ok(response)
    }

    @Operation(summary = "단과대 소속 학과 조회", description = "특정 단과대에 소속된 전체 학과 정보를 조회합니다.")
    @GetMapping("/colleges/{collegeId}/departments")
    fun readAllByCollegeId(
        @PathVariable collegeId: Long,
    ): ResponseEntity<List<ReadDepartmentsResponse>> {
        val result: ReadDepartmentsResult = departmentService.readAllByCollegeId(collegeId)
        val response: List<ReadDepartmentsResponse> = result.departmentDtos.map { ReadDepartmentsResponse.from(it) }

        return ResponseEntity.ok(response)
    }
}
