package com.yourssu.scouter.common.department.business

import com.yourssu.scouter.common.department.business.dto.ReadDepartmentsResult

import com.yourssu.scouter.common.department.implement.Department
import com.yourssu.scouter.common.department.implement.DepartmentReader
import org.springframework.stereotype.Service

@Service
class DepartmentService(
    private val departmentReader: DepartmentReader,
) {

    fun readAll(): ReadDepartmentsResult {
        val departments: List<Department> = departmentReader.readAll()

        return ReadDepartmentsResult.from(departments)
    }

    fun readAllByCollegeId(collegeId: Long): ReadDepartmentsResult {
        val departments: List<Department> = departmentReader.readAllByCollegeId(collegeId)

        return ReadDepartmentsResult.from(departments)
    }
}
