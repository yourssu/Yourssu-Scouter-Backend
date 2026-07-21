package com.yourssu.scouter.common.college.storage

import com.yourssu.scouter.common.college.implement.College
import com.yourssu.scouter.common.college.implement.CollegeRepository
import org.springframework.stereotype.Repository

@Repository
class CollegeRepositoryImpl(
    private val jpaCollegeRepository: JpaCollegeRepository,
) : CollegeRepository {

    override fun save(college: College): College = jpaCollegeRepository.save(CollegeEntity.from(college)).toDomain()

    override fun count(): Long = jpaCollegeRepository.count()

    override fun findAll(): List<College> = jpaCollegeRepository.findAll().map { it.toDomain() }
}
