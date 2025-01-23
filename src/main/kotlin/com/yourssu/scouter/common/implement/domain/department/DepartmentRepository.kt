package com.yourssu.scouter.common.implement.domain.department

interface DepartmentRepository {

    fun findAllByCollegeId(collegeId: Long): List<Department>
}
