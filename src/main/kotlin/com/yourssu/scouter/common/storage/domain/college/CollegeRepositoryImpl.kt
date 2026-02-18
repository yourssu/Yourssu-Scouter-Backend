package com.yourssu.scouter.common.storage.domain.college

import com.yourssu.scouter.common.implement.domain.college.College
import com.yourssu.scouter.common.implement.domain.college.CollegeRepository
import org.springframework.stereotype.Repository

@Repository
class CollegeRepositoryImpl(
    private val jpaCollegeRepository: JpaCollegeRepository,
) : CollegeRepository {

    override fun save(college: College): College = jpaCollegeRepository.save(CollegeEntity.from(college)).toDomain()

    override fun count(): Long = jpaCollegeRepository.count()

    override fun findAll(): List<College> = jpaCollegeRepository.findAll().map { it.toDomain() }
}
