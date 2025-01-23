package com.yourssu.scouter.common.business.domain.department

import com.yourssu.scouter.common.implement.domain.department.Department

data class DepartmentDto(
    val id: Long,
    val collegeId: Long,
    val name: String,
) {

    companion object {
        fun from(department: Department): DepartmentDto {
            return DepartmentDto(
                id = department.id!!,
                collegeId = department.collegeId,
                name = department.name,
            )
        }
    }
}
