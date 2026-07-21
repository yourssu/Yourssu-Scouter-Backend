package com.yourssu.scouter.common.department.business.dto

import com.yourssu.scouter.common.department.implement.Department

data class ReadDepartmentsResult(
    val departmentDtos: List<DepartmentDto>,
) {

    companion object {
        fun from(departments: List<Department>): ReadDepartmentsResult = ReadDepartmentsResult(
            departmentDtos = departments.map { DepartmentDto.from(it) },
        )
    }
}
