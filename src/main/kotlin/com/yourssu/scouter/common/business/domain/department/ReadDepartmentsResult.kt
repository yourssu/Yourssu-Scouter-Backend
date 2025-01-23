package com.yourssu.scouter.common.business.domain.department

import com.yourssu.scouter.common.implement.domain.department.Department

data class ReadDepartmentsResult(
    val departmentDtos: List<DepartmentDto>,
) {

    companion object {
        fun from(departments: List<Department>): ReadDepartmentsResult = ReadDepartmentsResult(
            departmentDtos = departments.map { DepartmentDto.from(it) },
        )
    }
}
