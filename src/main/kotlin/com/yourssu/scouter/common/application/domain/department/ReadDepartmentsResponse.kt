package com.yourssu.scouter.common.application.domain.department

import com.yourssu.scouter.common.business.domain.department.DepartmentDto

data class ReadDepartmentsResponse(
    val departmentId: Long,
    val departmentName: String,
) {

    companion object {
        fun from(departmentDto: DepartmentDto) = ReadDepartmentsResponse(
            departmentId = departmentDto.id,
            departmentName = departmentDto.name,
        )
    }
}
