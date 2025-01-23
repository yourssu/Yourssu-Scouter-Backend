package com.yourssu.scouter.common.application.domain.department

import com.yourssu.scouter.common.business.domain.department.DepartmentService
import com.yourssu.scouter.common.business.domain.department.ReadDepartmentsResult
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
class DepartmentController(
    private val departmentService: DepartmentService,
) {

    @GetMapping("/colleges/{collegeId}/departments")
    fun readAllByCollegeId(
        @PathVariable collegeId: Long,
    ): ResponseEntity<List<ReadDepartmentsResponse>> {
        val result: ReadDepartmentsResult = departmentService.readAllByCollegeId(collegeId)
        val response: List<ReadDepartmentsResponse> = result.departmentDtos.map { ReadDepartmentsResponse.from(it) }

        return ResponseEntity.ok(response)
    }
}
