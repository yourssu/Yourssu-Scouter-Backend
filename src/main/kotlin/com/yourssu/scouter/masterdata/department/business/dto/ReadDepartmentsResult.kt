package com.yourssu.scouter.masterdata.department.business.dto

import com.yourssu.scouter.masterdata.department.implement.Department

data class ReadDepartmentsResult(
    val departmentDtos: List<DepartmentDto>,
) {

    companion object {
        fun from(departments: List<Department>): ReadDepartmentsResult = ReadDepartmentsResult(
            departmentDtos = departments.map { DepartmentDto.from(it) },
        )
    }
}
