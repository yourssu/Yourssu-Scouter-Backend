package com.yourssu.scouter.common.storage.domain.department

import org.springframework.data.jpa.repository.JpaRepository

interface JpaDepartmentRepository : JpaRepository<DepartmentEntity, Long> {

    fun findAllByCollegeId(collegeId: Long): List<DepartmentEntity>
}
