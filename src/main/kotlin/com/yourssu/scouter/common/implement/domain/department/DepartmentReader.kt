package com.yourssu.scouter.common.implement.domain.department

import com.yourssu.scouter.common.implement.support.exception.DepartmentNotFoundException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Transactional(readOnly = true)
class DepartmentReader(
    private val departmentRepository: DepartmentRepository,
) {

    fun readById(departmentId: Long): Department = departmentRepository.findById(departmentId)
        ?: throw DepartmentNotFoundException("지정한 학과를 찾을 수 없습니다.")

    fun readAll(): List<Department> = departmentRepository.findAllByOrderByNameAsc()

    fun readAllByCollegeId(collegeId: Long): List<Department> = departmentRepository.findAllByCollegeId(collegeId)
}
