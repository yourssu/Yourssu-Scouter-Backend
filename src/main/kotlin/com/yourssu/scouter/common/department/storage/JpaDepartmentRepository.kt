package com.yourssu.scouter.common.department.storage

import org.springframework.data.jpa.repository.JpaRepository

interface JpaDepartmentRepository : JpaRepository<DepartmentEntity, Long> {

    fun findAllByOrderByNameAsc(): List<DepartmentEntity>
    fun findAllByCollegeId(collegeId: Long): List<DepartmentEntity>
}
