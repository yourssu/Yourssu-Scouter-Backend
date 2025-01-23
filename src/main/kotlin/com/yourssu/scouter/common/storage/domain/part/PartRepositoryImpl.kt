package com.yourssu.scouter.common.storage.domain.part

import com.yourssu.scouter.common.implement.domain.part.Part
import com.yourssu.scouter.common.implement.domain.part.PartRepository
import org.springframework.stereotype.Repository

@Repository
class PartRepositoryImpl(
    private val jpaPartRepository: JpaPartRepository,
) : PartRepository {

    override fun findAll(): List<Part> {
        return jpaPartRepository.findAll().map { it.toDomain() }
    }
}
