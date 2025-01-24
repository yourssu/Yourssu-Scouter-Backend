package com.yourssu.scouter.common.implement.domain.department

interface DepartmentRepository {

    fun saveAll(departments: List<Department>)
    fun findById(id: Long): Department?
    fun findAllByCollegeId(collegeId: Long): List<Department>
}
