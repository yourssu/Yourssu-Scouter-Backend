package com.yourssu.scouter.common.business.domain.department

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentReader
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
