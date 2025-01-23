package com.yourssu.scouter.common.storage.domain.college

import com.yourssu.scouter.common.implement.domain.college.College
import com.yourssu.scouter.common.implement.domain.college.CollegeRepository
import org.springframework.stereotype.Repository

@Repository
class CollegeRepositoryImpl(
    private val jpaCollegeRepository: JpaCollegeRepository,
) : CollegeRepository {

    override fun saveAll(colleges: List<College>) {
        jpaCollegeRepository.saveAll(colleges.map { CollegeEntity.from(it) })
    }

    override fun count(): Long {
        return jpaCollegeRepository.count()
    }

    override fun findAll(): List<College> {
        return jpaCollegeRepository.findAll().map { it.toDomain() }
    }
}
