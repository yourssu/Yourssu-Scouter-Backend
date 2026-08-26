package com.yourssu.scouter.masterdata.department.storage

import com.yourssu.scouter.masterdata.department.implement.Department
import com.yourssu.scouter.masterdata.department.implement.DepartmentRepository
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

    override fun findAllByOrderByNameAsc(): List<Department> =
        jpaDepartmentRepository.findAllByOrderByNameAsc().map { it.toDomain() }

    override fun findAllByCollegeId(collegeId: Long): List<Department> =
        jpaDepartmentRepository.findAllByCollegeId(collegeId).map { it.toDomain() }
}
