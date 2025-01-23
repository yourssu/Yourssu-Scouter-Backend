package com.yourssu.scouter.common.storage.domain.college

import com.yourssu.scouter.common.implement.domain.college.College
import com.yourssu.scouter.common.implement.domain.college.CollegeRepository
import org.springframework.stereotype.Repository

@Repository
class CollegeRepositoryImpl(
    private val jpaCollegeRepository: JpaCollegeRepository,
) : CollegeRepository {

    override fun findAll(): List<College> {
        return jpaCollegeRepository.findAll().map { it.toDomain() }
    }
}
