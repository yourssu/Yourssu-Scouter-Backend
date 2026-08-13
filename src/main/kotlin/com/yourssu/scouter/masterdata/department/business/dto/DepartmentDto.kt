package com.yourssu.scouter.masterdata.department.business.dto

import com.yourssu.scouter.masterdata.department.implement.Department

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
