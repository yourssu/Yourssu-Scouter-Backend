package com.yourssu.scouter.common.department.application

import com.yourssu.scouter.common.department.business.DepartmentDto

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
