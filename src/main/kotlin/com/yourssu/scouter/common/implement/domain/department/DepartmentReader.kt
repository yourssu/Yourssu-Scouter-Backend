package com.yourssu.scouter.common.implement.domain.department

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class DepartmentReader(
    private val departmentRepository: DepartmentRepository,
) {

    fun readAllByCollegeId(collegeId: Long): List<Department> = departmentRepository.findAllByCollegeId(collegeId)
}
