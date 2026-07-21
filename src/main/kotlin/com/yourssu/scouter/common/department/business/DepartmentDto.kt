package com.yourssu.scouter.common.department.business

import com.yourssu.scouter.common.department.implement.Department

data class DepartmentDto(
    val id: Long,
    val collegeId: Long,
    val name: String,
) {

    companion object {
        fun from(department: Department): DepartmentDto = DepartmentDto(
            id = department.id!!,
            collegeId = department.collegeId,
            name = department.name,
        )
    }
}
