package com.yourssu.scouter.common.storage.domain.department

import com.yourssu.scouter.common.implement.domain.department.Department
import com.yourssu.scouter.common.implement.domain.department.DepartmentRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class DepartmentRepositoryImpl(
    private val jpaDepartmentRepository: JpaDepartmentRepository,
) : DepartmentRepository {

    override fun saveAll(departments: List<Department>) {
        jpaDepartmentRepository.saveAll(departments.map { DepartmentEntity.from(it) })
    }

    override fun findById(id: Long): Department? =
        jpaDepartmentRepository.findByIdOrNull(id)?.toDomain()

    override fun findAllOrderByName(): List<Department> {
        return jpaDepartmentRepository.findAllOrderByName().map { it.toDomain() }
    }

    override fun findAllByCollegeId(collegeId: Long): List<Department> =
        jpaDepartmentRepository.findAllByCollegeId(collegeId).map { it.toDomain() }
}
