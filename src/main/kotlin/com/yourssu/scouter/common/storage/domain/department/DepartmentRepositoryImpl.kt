package com.yourssu.scouter.common.storage.domain.department

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentRepository
import org.springframework.stereotype.Repository

@Repository
class DepartmentRepositoryImpl(
    private val jpaDepartmentRepository: JpaDepartmentRepository,
) : DepartmentRepository {

    override fun findAllByCollegeId(collegeId: Long): List<Department> {
        return jpaDepartmentRepository.findAllByCollegeId(collegeId).map { it.toDomain() }
    }
}
